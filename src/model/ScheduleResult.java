package model;

import java.util.ArrayList;
import java.util.List;

public class ScheduleResult {

    private List<GanttSegment> ganttChart;
    private List<Process> processes;

    private double averageWaitingTime;
    private double averageTurnaroundTime;
    private double averageResponseTime;

    public ScheduleResult() {
        ganttChart = new ArrayList<>();
        processes = new ArrayList<>();
    }

    // Getters

    public List<GanttSegment> getGanttChart() {
        return ganttChart;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public double getAverageTurnaroundTime() {
        return averageTurnaroundTime;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    // Setters

    public void setGanttChart(List<GanttSegment> ganttChart) {
        this.ganttChart = ganttChart;
    }

    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    public void setAverageWaitingTime(double averageWaitingTime) {
        this.averageWaitingTime = averageWaitingTime;
    }

    public void setAverageTurnaroundTime(double averageTurnaroundTime) {
        this.averageTurnaroundTime = averageTurnaroundTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
}