package gui;

import model.GanttSegment;
import model.Process;
import model.ScheduleResult;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Shows the Time | Process | Tier matrix in a table.
 * The static methods build the matrix rows from a ScheduleResult;
 * Main uses them too, to print the same matrix in the console.
 */
public class MatrixPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Time", "Process", "Tiers & events"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };

    private final JTable table = new JTable(model);

    public MatrixPanel() {
        super(new BorderLayout());
        setOpaque(false);
        MainFrame.styleTable(table);
        table.setRowHeight(24);
        table.setShowHorizontalLines(false);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // long Tier lines scroll sideways instead of "..."
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.setDefaultRenderer(Object.class, new LineRenderer());
        add(MainFrame.scroll(table));
    }

    /** Colors each line by what it says, and shades every other time step so they are easy to tell apart. */
    private class LineRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, false, row, col);
            String text = value == null ? "" : value.toString();
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(col == 2 ? LEFT : CENTER);
            setFont(col < 2 && !text.isEmpty() ? MainFrame.BOLD : t.getFont());
            if (selected) return this;

            // find the time step this row belongs to
            int step = row;
            while (step > 0 && model.getValueAt(step, 0).toString().isEmpty()) step--;
            boolean cs = model.getValueAt(step, 1).equals("CS");
            int timeValue = Integer.parseInt(model.getValueAt(step, 0).toString());
            setBackground(cs ? new Color(0xFFF7E6) : timeValue % 2 == 0 ? Color.WHITE : new Color(0xF7F9FC));

            Color fg = MainFrame.TEXT;
            if (col == 2) {
                if (text.startsWith("Tier 1")) fg = new Color(0x1D4ED8);
                else if (text.startsWith("Tier 2")) fg = new Color(0x7C3AED);
                else if (text.startsWith("Context switch")) fg = new Color(0xB45309);
                else if (text.contains("Waited from")) fg = new Color(0x047857);
            } else if (col == 1 && cs) {
                fg = new Color(0xB45309);
            }
            setForeground(fg);
            return this;
        }
    }

    public void setRows(List<String[]> rows) {
        model.setRowCount(0);
        int longest = 0;
        FontMetrics fm = table.getFontMetrics(table.getFont());
        for (String[] row : rows) {
            model.addRow(row);
            longest = Math.max(longest, fm.stringWidth(row[2]));
        }
        table.getColumnModel().getColumn(2).setPreferredWidth(Math.max(longest + 20, 500));
    }

    // ---------------- building the matrix ----------------

    // cpu[t] = what runs from t to t+1 (process name, CS or Idle)
    public static String[] timeline(ScheduleResult r) {
        int end = 0;
        for (Process p : r.getProcesses()) end = Math.max(end, p.getCompletionTime());
        String[] cpu = new String[end];
        Arrays.fill(cpu, "Idle");
        for (GanttSegment g : r.getGanttChart())
            for (int t = g.getStartTime(); t < g.getEndTime() && t < end; t++)
                cpu[t] = g.getType() == GanttSegment.SegmentType.PROCESS ? g.getProcessId()
                        : g.getType() == GanttSegment.SegmentType.CONTEXT_SWITCH ? "CS" : "Idle";
        return cpu;
    }

    static int remaining(Process p, int t, String[] cpu) {
        int used = 0;
        for (int i = 0; i < t; i++) if (cpu[i].equals(p.getId())) used++;
        return p.getBurstTime() - used;
    }

    // the time the scheduler picked a process (before its context switch)
    static int picked(int start, String[] cpu) {
        while (start > 0 && cpu[start - 1].equals("CS")) start--;
        return start;
    }

    /** SJF: one row per time unit with the running process. */
    public static List<String[]> sjfRows(ScheduleResult r) {
        String[] cpu = timeline(r);
        List<String[]> rows = new ArrayList<>();
        for (int t = 0; t < cpu.length; t++) rows.add(new String[]{"" + t, cpu[t], ""});
        rows.add(new String[]{"" + cpu.length, "", "All processes finished"});
        return rows;
    }

    /** ACS: one row per line; the time and process are only on the first line of each time step. */
    public static List<String[]> acsRows(ScheduleResult r, int q, int aging) {
        String[] cpu = timeline(r);
        List<Process> ps = r.getProcesses();
        int k = ps.size();
        int[] pick1 = new int[k], demote = new int[k], pick2 = new int[k];
        for (int i = 0; i < k; i++) {
            Process p = ps.get(i);
            pick1[i] = picked(p.getStartTime(), cpu);
            boolean toTier2 = p.getBurstTime() > q;
            demote[i] = toTier2 ? p.getStartTime() + q : -1;
            pick2[i] = toTier2 ? picked(p.getCompletionTime() - (p.getBurstTime() - q), cpu) : -1;
        }

        List<String[]> rows = new ArrayList<>();
        boolean tier2Used = false;
        for (int t = 0; t <= cpu.length; t++) {
            String running = t < cpu.length ? cpu[t] : "";
            List<String> lines = new ArrayList<>();

            boolean event = t == cpu.length;
            for (int i = 0; i < k; i++)
                if (ps.get(i).getArrivalTime() == t || ps.get(i).getCompletionTime() == t
                        || pick1[i] == t || demote[i] == t || pick2[i] == t) event = true;

            if (event) {
                if (running.equals("CS")) {
                    int j = t;
                    while (cpu[j].equals("CS")) j++;
                    boolean saves = false; // previous process is not finished yet
                    for (Process p : ps) if (t > 0 && p.getId().equals(cpu[t - 1]) && p.getCompletionTime() > t) saves = true;
                    lines.add("Context switch " + (saves ? "saves " + cpu[t - 1] + " and " : "") + "loads " + cpu[j]);
                }

                StringJoiner tier1 = new StringJoiner(", ");
                for (int i = 0; i < k; i++)
                    if (ps.get(i).getArrivalTime() <= t && t <= pick1[i])
                        tier1.add(ps.get(i).getId() + " (remaining: " + remaining(ps.get(i), t, cpu) + ", Priority: " + ps.get(i).getPriority() + ")");

                boolean agingNow = false;
                for (int i = 0; i < k; i++) if (pick2[i] == t && tier1.length() == 0) agingNow = true;

                StringJoiner tier2 = new StringJoiner(", ");
                for (int time = 0; time <= t; time++)
                    for (int i = 0; i < k; i++) {
                        if (demote[i] != time || t > pick2[i]) continue;
                        Process p = ps.get(i);
                        int waited = t - demote[i], a = agingNow ? waited / aging : 0;
                        if (agingNow) lines.add(p.getId() + ": Waited from T=" + demote[i] + " to T=" + t + " (" + waited + " units). "
                                + (a > 0 ? "Aging: floor(" + waited + "/" + aging + ") = " + a + ". Effective Priority = " + p.getPriority() + " - " + a + " = " : "Effective Priority = ")
                                + (p.getPriority() - a));
                        tier2.add(p.getId() + " (remaining: " + remaining(p, t, cpu) + ", Priority: " + (p.getPriority() - a) + ")");
                        tier2Used = true;
                    }

                lines.add("Tier 1: [" + (tier1.length() == 0 ? "Empty" : tier1) + "]");
                if (tier2Used) lines.add("Tier 2: [" + (tier2.length() == 0 ? "Empty" : tier2) + "]");
            }

            if (lines.isEmpty()) lines.add("");
            for (int i = 0; i < lines.size(); i++)
                rows.add(new String[]{i == 0 ? "" + t : "", i == 0 ? running : "", lines.get(i)});
        }
        return rows;
    }
}
