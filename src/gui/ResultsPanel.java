package gui;

import model.Process;
import model.ScheduleResult;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ResultsPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Process", "Arrival", "Burst", "Finish", "Turnaround", "Waiting", "Response"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JLabel avgWaiting = statValue();
    private final JLabel avgTurnaround = statValue();
    private final JLabel avgResponse = statValue();

    public ResultsPanel() {
        super(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.add(stat("Avg waiting time", avgWaiting));
        stats.add(stat("Avg turnaround time", avgTurnaround));
        stats.add(stat("Avg response time", avgResponse));

        JTable table = new JTable(model);
        MainFrame.styleTable(table);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < model.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(center);

        add(stats, BorderLayout.NORTH);
        add(MainFrame.scroll(table), BorderLayout.CENTER);
    }

    private static JLabel statValue() {
        JLabel l = new JLabel("–");
        l.setFont(MainFrame.BOLD.deriveFont(22f));
        l.setForeground(MainFrame.ACCENT);
        return l;
    }

    private static JPanel stat(String title, JLabel value) {
        JLabel t = new JLabel(title);
        t.setFont(MainFrame.FONT.deriveFont(12f));
        t.setForeground(MainFrame.MUTED);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0xF5F8FE));
        p.setBorder(BorderFactory.createCompoundBorder(new MainFrame.RoundBorder(), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    public void setResult(ScheduleResult r) {
        model.setRowCount(0);
        for (Process p : r.getProcesses()) {
            model.addRow(new Object[]{p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getCompletionTime(),
                    p.getTurnaroundTime(), p.getWaitingTime(), p.getResponseTime()});
        }
        avgWaiting.setText(String.format("%.2f", r.getAverageWaitingTime()));
        avgTurnaround.setText(String.format("%.2f", r.getAverageTurnaroundTime()));
        avgResponse.setText(String.format("%.2f", r.getAverageResponseTime()));
    }
}
