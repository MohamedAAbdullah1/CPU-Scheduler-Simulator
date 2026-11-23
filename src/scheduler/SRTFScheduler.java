package scheduler;

import model.Process;
import model.GanttSegment;
import model.ScheduleResult;
import java.util.List;
import java.util.ArrayList;

public class SRTFScheduler implements Scheduler {

    private Process selectShortestRemaining(List<Process> processes, int currentTime) {

        Process shortest = null;

        for (Process p : processes) {
            // to ensure all processes arrived and not finished
            if (p.getArrivalTime() <= currentTime && !p.isFinished()) {

                if (shortest == null ||
                        p.getRemainingTime() < shortest.getRemainingTime()) {

                    shortest = p;
                }
            }
        }

        return shortest;
    }

    private void addContextSwitch(List<GanttSegment> ganttChart, int currentTime, int contextSwitch) {

        if (contextSwitch > 0) {

            ganttChart.add(
                    new GanttSegment(
                            null,
                            currentTime,
                            currentTime + contextSwitch,
                            GanttSegment.SegmentType.CONTEXT_SWITCH));
        }
    }

    private void addProcessSegment(
            List<GanttSegment> ganttChart,
            Process process,
            int currentTime) {

        if (!ganttChart.isEmpty()) {

            GanttSegment last = ganttChart.get(ganttChart.size() - 1);

            if (last.getType() == GanttSegment.SegmentType.PROCESS &&
                    last.getProcessId().equals(process.getId()) &&
                    last.getEndTime() == currentTime) {

                last.setEndTime(currentTime + 1);
                return;
            }
        }

        ganttChart.add(
                new GanttSegment(
                        process.getId(),
                        currentTime,
                        currentTime + 1,
                        GanttSegment.SegmentType.PROCESS));
    }

    @Override
    public ScheduleResult run(List<Process> processes, int contextSwitch) {

        List<GanttSegment> ganttChart = new ArrayList<>();

        int currentTime = 0;
        int completed = 0;

        Process lastProcess = null;

        while (completed < processes.size()) {

            Process current = selectShortestRemaining(processes, currentTime);

            // No process is available
            if (current == null) {

                if (!ganttChart.isEmpty()) {

                    GanttSegment last = ganttChart.get(ganttChart.size() - 1);

                    if (last.getType() == GanttSegment.SegmentType.IDLE &&
                            last.getEndTime() == currentTime) {

                        last.setEndTime(currentTime + 1);

                    } else {

                        ganttChart.add(
                                new GanttSegment(
                                        null,
                                        currentTime,
                                        currentTime + 1,
                                        GanttSegment.SegmentType.IDLE));
                    }

                } else {

                    ganttChart.add(
                            new GanttSegment(
                                    null,
                                    currentTime,
                                    currentTime + 1,
                                    GanttSegment.SegmentType.IDLE));
                }

                currentTime++;
                lastProcess = null;

                continue;
            }

            // Context switching
            if (lastProcess != null && lastProcess != current) {

                addContextSwitch(ganttChart, currentTime, contextSwitch);

                currentTime += contextSwitch;

                // Re-evaluate because new processes might have arrived during CS
                current = selectShortestRemaining(processes, currentTime);
            }

            // Record first execution time
            if (current.getStartTime() == -1) {
                current.setStartTime(currentTime);
            }

            // Execute for one time unit
            addProcessSegment(ganttChart, current, currentTime);

            current.execute();

            currentTime++;

            // Check completion
            if (current.isFinished()) {
                current.setCompletionTime(currentTime);
                completed++;
            }

            lastProcess = current;
        }

        // Calculate averages
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        double totalResponseTime = 0;

        for (Process p : processes) {

            totalWaitingTime += p.getWaitingTime();
            totalTurnaroundTime += p.getTurnaroundTime();
            totalResponseTime += p.getResponseTime();
        }

        ScheduleResult result = new ScheduleResult();

        result.setGanttChart(ganttChart);
        result.setProcesses(processes);
        if (processes == null || processes.isEmpty()) {
            return new ScheduleResult();
        }
        result.setAverageWaitingTime(
                totalWaitingTime / processes.size());

        result.setAverageTurnaroundTime(
                totalTurnaroundTime / processes.size());

        result.setAverageResponseTime(
                totalResponseTime / processes.size());

        return result;
    }

}