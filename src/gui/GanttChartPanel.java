package gui;

import model.GanttSegment;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class GanttChartPanel extends JPanel {

    private static final Color[] COLORS = {
            new Color(0x3B82F6), new Color(0xF59E0B), new Color(0x10B981), new Color(0xEF4444),
            new Color(0x8B5CF6), new Color(0x06B6D4), new Color(0xEC4899), new Color(0x84CC16)
    };
    private static final Color CS_COLOR = new Color(0x94A3B8);

    private List<GanttSegment> segments = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public GanttChartPanel() {
        setPreferredSize(new Dimension(800, 95));
        setOpaque(false);
    }

    public void setSegments(List<GanttSegment> segments) {
        this.segments = segments;
        TreeSet<String> set = new TreeSet<>();
        for (GanttSegment s : segments) if (s.getType() == GanttSegment.SegmentType.PROCESS) set.add(s.getProcessId());
        ids = new ArrayList<>(set);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(MainFrame.BOLD);

        if (segments.isEmpty()) {
            g.setColor(MainFrame.MUTED);
            g.setFont(MainFrame.FONT);
            g.drawString("Enter the processes and press \"Run simulation\".", 8, 50);
            return;
        }

        int end = segments.get(segments.size() - 1).getEndTime();
        int left = 8, top = 8, height = 40;
        double unit = (getWidth() - 2.0 * left) / Math.max(end, 1);
        FontMetrics fm = g.getFontMetrics();

        int lastLabelX = -100;
        for (GanttSegment s : segments) {
            int x = left + (int) Math.round(s.getStartTime() * unit);
            int w = (int) Math.round(s.getDuration() * unit) - 2;
            String label;
            if (s.getType() == GanttSegment.SegmentType.PROCESS) {
                g.setColor(colorFor(s.getProcessId()));
                g.fillRoundRect(x, top, w, height, 10, 10);
                label = s.getProcessId();
                g.setColor(Color.WHITE);
            } else if (s.getType() == GanttSegment.SegmentType.CONTEXT_SWITCH) {
                g.setColor(CS_COLOR);
                g.fillRoundRect(x, top, w, height, 10, 10);
                label = "CS";
                g.setColor(Color.WHITE);
            } else {
                g.setColor(new Color(0xE5E7EB));
                g.fillRoundRect(x, top, w, height, 10, 10);
                label = "Idle";
                g.setColor(MainFrame.MUTED);
            }
            if (fm.stringWidth(label) < w - 4)
                g.drawString(label, x + (w - fm.stringWidth(label)) / 2, top + height / 2 + 5);

            lastLabelX = drawTime(g, s.getStartTime(), x, top + height + 16, lastLabelX);
        }
        drawTime(g, end, left + (int) Math.round(end * unit) - 2, top + height + 16, lastLabelX);

        g.setFont(MainFrame.FONT);
        int lx = left, ly = top + height + 40;
        for (String id : ids) lx = legendItem(g, colorFor(id), id, lx, ly);
        legendItem(g, CS_COLOR, "Context switch", lx, ly);
    }

    private int drawTime(Graphics2D g, int time, int x, int y, int lastX) {
        g.setFont(MainFrame.FONT.deriveFont(11f));
        g.setColor(MainFrame.MUTED);
        String t = "" + time;
        int tx = x - g.getFontMetrics().stringWidth(t) / 2;
        if (tx - lastX < 6) return lastX;
        g.drawString(t, Math.max(tx, 0), y);
        g.setFont(MainFrame.BOLD);
        return tx + g.getFontMetrics().stringWidth(t);
    }

    private int legendItem(Graphics2D g, Color c, String text, int x, int y) {
        g.setColor(c);
        g.fillRoundRect(x, y - 10, 12, 12, 4, 4);
        g.setColor(MainFrame.TEXT);
        g.drawString(text, x + 17, y);
        return x + 30 + g.getFontMetrics().stringWidth(text);
    }

    private Color colorFor(String id) {
        int i = ids.indexOf(id);
        return COLORS[(i < 0 ? 0 : i) % COLORS.length];
    }
}
