package gui;

import model.ScheduleResult;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SimulationPanel extends JPanel {

    private final GanttChartPanel gantt = new GanttChartPanel();
    private final MatrixPanel matrix = new MatrixPanel();
    private final ResultsPanel results = new ResultsPanel();

    public SimulationPanel() {
        super(new BorderLayout(0, 12));
        setBackground(MainFrame.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                MainFrame.card("Time-step matrix", matrix), MainFrame.card("Results", results));
        split.setResizeWeight(0.7);
        split.setBorder(null);
        split.setDividerSize(10);
        split.setOpaque(false);

        add(MainFrame.card("Gantt chart", gantt), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    public void display(ScheduleResult result, List<String[]> matrixRows) {
        gantt.setSegments(result.getGanttChart());
        matrix.setRows(matrixRows);
        results.setResult(result);
    }
}
