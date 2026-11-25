package gui;

import javax.swing.*;
import java.awt.*;

public class InputPanel extends JPanel {

    private final ProcessTablePanel processTable = new ProcessTablePanel();
    private final JSpinner quantum = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JSpinner contextSwitch = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner aging = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
    private final JButton run = MainFrame.button("Run simulation", true);

    public InputPanel() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        setPreferredSize(new Dimension(340, 0));

        JPanel settings = new JPanel(new GridBagLayout());
        settings.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 4, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        addSetting(settings, c, 0, "Quantum (Q)", quantum);
        addSetting(settings, c, 1, "Context switch (CS)", contextSwitch);
        addSetting(settings, c, 2, "Aging interval", aging);

        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setOpaque(false);
        bottom.add(MainFrame.card("Settings", settings), BorderLayout.CENTER);
        bottom.add(run, BorderLayout.SOUTH);

        add(MainFrame.card("Processes", processTable), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private static void addSetting(JPanel p, GridBagConstraints c, int row, String label, JSpinner spinner) {
        JLabel l = new JLabel(label);
        l.setFont(MainFrame.FONT);
        l.setForeground(MainFrame.MUTED);
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 1;
        p.add(l, c);
        c.gridx = 1;
        c.weightx = 0;
        spinner.setPreferredSize(new Dimension(90, 28));
        spinner.setFont(MainFrame.FONT);
        p.add(spinner, c);
    }

    public ProcessTablePanel getProcessTable() { return processTable; }
    public int getQuantum() { return (Integer) quantum.getValue(); }
    public int getContextSwitch() { return (Integer) contextSwitch.getValue(); }
    public int getAgingInterval() { return (Integer) aging.getValue(); }
    public void onRun(Runnable action) { run.addActionListener(e -> action.run()); }
}
