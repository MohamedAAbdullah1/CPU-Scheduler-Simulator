package gui;

import model.Process;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessTablePanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Name", "Arrival", "Burst", "Priority"}, 0);
    private final JTable table = new JTable(model);

    public ProcessTablePanel() {
        super(new BorderLayout(0, 8));
        setOpaque(false);

        MainFrame.styleTable(table);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 4; i++) table.getColumnModel().getColumn(i).setCellRenderer(center);

        JLabel hint = new JLabel("Add a row per process, then double-click a cell to edit it.");
        hint.setFont(MainFrame.FONT.deriveFont(12f));
        hint.setForeground(MainFrame.MUTED);

        JButton addButton = MainFrame.button("+ Add process", false);
        JButton removeButton = MainFrame.button("Remove selected", false);
        addButton.addActionListener(e -> model.addRow(new Object[]{"P" + (model.getRowCount() + 1), "0", "1", "1"}));
        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) model.removeRow(row);
        });

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(removeButton);

        add(hint, BorderLayout.NORTH);
        add(MainFrame.scroll(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public List<Process> getProcesses() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        if (model.getRowCount() == 0) throw new IllegalArgumentException("Add at least one process.");

        List<Process> list = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            String name = model.getValueAt(r, 0).toString().trim();
            int arrival = number(r, 1), burst = number(r, 2), priority = number(r, 3);
            if (name.isEmpty()) throw new IllegalArgumentException("Row " + (r + 1) + ": name is empty.");
            if (arrival < 0 || burst <= 0)
                throw new IllegalArgumentException("Row " + (r + 1) + ": arrival must be >= 0 and burst > 0.");
            list.add(new Process(name, arrival, burst, priority));
        }
        return list;
    }

    private int number(int row, int col) {
        try {
            return Integer.parseInt(model.getValueAt(row, col).toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Row " + (row + 1) + ": \"" + model.getColumnName(col) + "\" must be a whole number.");
        }
    }
}
