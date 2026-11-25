package gui;

import model.ScheduleResult;
import scheduler.ACSScheduler;
import scheduler.SRTFScheduler;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** The main window: header on top, input on the left, one tab per scheduler on the right. */
public class MainFrame extends JFrame {

    // ---------------- shared theme ----------------
    static final Color BG = new Color(0xF1F4F9);
    static final Color CARD = Color.WHITE;
    static final Color BORDER = new Color(0xDCE2EB);
    static final Color ACCENT = new Color(0x2F6FEB);
    static final Color HEADER = new Color(0x1E293B);
    static final Color TEXT = new Color(0x1F2937);
    static final Color MUTED = new Color(0x6B7280);
    static final Font FONT = new Font("SansSerif", Font.PLAIN, 13);
    static final Font BOLD = FONT.deriveFont(Font.BOLD);

    /** A white rounded card with an optional title. */
    static JPanel card(String title, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(), BorderFactory.createEmptyBorder(10, 12, 12, 12)));
        if (title != null) {
            JLabel l = new JLabel(title);
            l.setFont(BOLD.deriveFont(14f));
            l.setForeground(TEXT);
            p.add(l, BorderLayout.NORTH);
        }
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(BORDER));
        s.getViewport().setBackground(CARD);
        return s;
    }

    static void styleTable(JTable t) {
        t.setRowHeight(26);
        t.setFont(FONT);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xEEF1F5));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(0xDCE8FD));
        t.setSelectionForeground(TEXT);
        JTableHeader h = t.getTableHeader();
        h.setFont(BOLD);
        h.setReorderingAllowed(false);
    }

    static JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(primary ? BOLD.deriveFont(14f) : FONT);
        b.setFocusPainted(false);
        if (primary) {
            b.setBackground(ACCENT);
            b.setForeground(Color.WHITE);
            b.setPreferredSize(new Dimension(0, 40));
        }
        return b;
    }

    /** Rounded light-grey outline used by the cards. */
    static class RoundBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g0, int x, int y, int w, int h) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(BORDER);
            g.drawRoundRect(x, y, w - 1, h - 1, 14, 14);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
    }

    static void applyTheme() {
        try {
            UIManager.put("control", BG);
            UIManager.put("nimbusBase", new Color(0x3B4A63));
            UIManager.put("nimbusBlueGrey", new Color(0xC9D1DD));
            UIManager.put("nimbusFocus", ACCENT);
            UIManager.put("nimbusSelectionBackground", ACCENT);
            UIManager.put("text", TEXT);
            UIManager.put("Table.alternateRowColor", new Color(0xF7F9FC));
            UIManager.put("defaultFont", FONT);
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if (info.getName().equals("Nimbus")) UIManager.setLookAndFeel(info.getClassName());
        } catch (Exception ignored) {
            // keep the default look if Nimbus is not available
        }
    }

    // ---------------- window ----------------

    private final InputPanel input = new InputPanel();
    private final SimulationPanel sjfPanel = new SimulationPanel();
    private final SimulationPanel acsPanel = new SimulationPanel();

    public MainFrame() {
        super("CPU Scheduler Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("CPU Scheduler Simulator");
        title.setFont(BOLD.deriveFont(20f));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Preemptive SJF  ·  Adaptive Container Scheduler (ACS)");
        subtitle.setFont(FONT);
        subtitle.setForeground(new Color(0xA5B4CB));
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(BOLD);
        tabs.addTab("  Preemptive SJF  ", sjfPanel);
        tabs.addTab("  ACS  ", acsPanel);
        tabs.setSelectedIndex(1);

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setBackground(BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        body.add(input, BorderLayout.WEST);
        body.add(tabs, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        input.onRun(this::runSimulation);

        setSize(1350, 820);
        setLocationRelativeTo(null);
    }

    private void runSimulation() {
        try {
            int q = input.getQuantum(), cs = input.getContextSwitch(), aging = input.getAgingInterval();

            ScheduleResult sjf = new SRTFScheduler().run(input.getProcessTable().getProcesses(), cs);
            sjfPanel.display(sjf, MatrixPanel.sjfRows(sjf));

            ScheduleResult acs = new ACSScheduler(q, aging).run(input.getProcessTable().getProcesses(), cs);
            acsPanel.display(acs, MatrixPanel.acsRows(acs, q, aging));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid input", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> {
            applyTheme();
            new MainFrame().setVisible(true);
        });
    }
}
