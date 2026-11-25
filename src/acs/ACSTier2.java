package acs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.GanttSegment;
import model.Process;
import model.ScheduleResult;
import scheduler.Scheduler;

public class ACSTier2 implements Scheduler {

    private final int agingInterval;

    private final AgingManager agingManager;

    public ACSTier2(int agingInterval) {
        if (agingInterval <= 0) {
            throw new IllegalArgumentException(
                "agingInterval cannot be less than 0"
            );
        }
        this.agingInterval = agingInterval;
        this.agingManager = new AgingManager(agingInterval);
    }

    @Override
    public ScheduleResult run(List<Process> processes, int contextSwitch) {
        ScheduleResult res = new ScheduleResult();

        if (processes == null || processes.isEmpty())
            return res;

        if (contextSwitch < 0) {
            throw new IllegalArgumentException(
                "contextSwitch cannot be less than 0"
            );
        }

        List<Process> sortedProcesses = new ArrayList<>(processes);
        sortedProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));

        List<Process> readyList = new ArrayList<>();
        List<GanttSegment> ganttChart = new ArrayList<>();

        int currentTime = 0;
        int nextArrival = 0;
        Process lastProcess = null;

        if (sortedProcesses.get(0).getArrivalTime() > 0) {
            int earliestTier2Entry = getEarliestTier2Entry(sortedProcesses);
            if (earliestTier2Entry > 0 && earliestTier2Entry < sortedProcesses.get(0).getArrivalTime()) {
                currentTime = earliestTier2Entry;
            }
        }

        while (nextArrival < sortedProcesses.size()
                && sortedProcesses.get(nextArrival).getArrivalTime() <= currentTime) {
            Process p = sortedProcesses.get(nextArrival);
            ensureTier2EntryTime(p, currentTime);
            readyList.add(p);
            nextArrival++;
        }

        while (!readyList.isEmpty() || nextArrival < sortedProcesses.size()) {
            if (readyList.isEmpty()) {
                int nextArrivalTime = sortedProcesses.get(nextArrival).getArrivalTime();
                addIdleSegment(ganttChart, currentTime, nextArrivalTime);
                currentTime = nextArrivalTime;
                lastProcess = null;

                while (nextArrival < sortedProcesses.size()
                        && sortedProcesses.get(nextArrival).getArrivalTime() <= currentTime) {
                    Process p = sortedProcesses.get(nextArrival);
                    ensureTier2EntryTime(p, currentTime);
                    readyList.add(p);
                    nextArrival++;
                }
                continue;
            }

            agingManager.applyAging(readyList, currentTime);

            Process currentProcess = selectHighestPriorityProcess(readyList);
            readyList.remove(currentProcess);

            if (lastProcess != null && lastProcess != currentProcess) {
                addContextSwitch(ganttChart, currentTime, contextSwitch);
                currentTime += contextSwitch;

                while (nextArrival < sortedProcesses.size()
                        && sortedProcesses.get(nextArrival).getArrivalTime() <= currentTime) {
                    Process p = sortedProcesses.get(nextArrival);
                    ensureTier2EntryTime(p, currentTime);
                    readyList.add(p);
                    nextArrival++;
                }
            }
            agingManager.updateEffectivePriority(currentProcess, currentTime);

            if (currentProcess.getStartTime() == -1) {
                currentProcess.setStartTime(currentTime);
            }

            int executionDuration = currentProcess.getRemainingTime();
            int executionStart = currentTime;
            int executionEnd = currentTime + executionDuration;

            addProcessSegment(ganttChart, currentProcess, executionStart, executionEnd);

            currentProcess.setRemainingTime(0);
            currentTime = executionEnd;
            currentProcess.setCompletionTime(currentTime);

            while (nextArrival < sortedProcesses.size()
                    && sortedProcesses.get(nextArrival).getArrivalTime() <= currentTime) {
                Process p = sortedProcesses.get(nextArrival);
                ensureTier2EntryTime(p, currentTime);
                readyList.add(p);
                nextArrival++;
            }

            lastProcess = currentProcess;
        }

        res.setGanttChart(ganttChart);
        res.setProcesses(processes);
        return res;
    }

    private void ensureTier2EntryTime(Process process, int currentTime) {
        if (process.getTier() != 2) {
            process.setTier(2);
        }
        if (process.getTier2EntryTime() < 0) {
            process.setTier2EntryTime(Math.max(process.getArrivalTime(), currentTime));
        }
    }

    private int getEarliestTier2Entry(List<Process> processes) {
        int earliest = Integer.MAX_VALUE;
        for (Process p : processes) {
            if (p.getTier2EntryTime() >= 0 && p.getTier2EntryTime() < earliest) {
                earliest = p.getTier2EntryTime();
            }
        }
        if(earliest == Integer.MAX_VALUE)
            return -1;
        else
            return earliest;
    }
    private Process selectHighestPriorityProcess(List<Process> readyProcesses) {
        Process best = readyProcesses.get(0);
        for (int i = 1; i < readyProcesses.size(); i++) {
            Process candidate = readyProcesses.get(i);

            if (candidate.getEffectivePriority() != best.getEffectivePriority()) 
                if (candidate.getEffectivePriority() < best.getEffectivePriority()) 
                    best = candidate;
                
            else if (candidate.getArrivalTime() != best.getArrivalTime()) 
                if (candidate.getArrivalTime() < best.getArrivalTime()) 
                    best = candidate;
                
            else if (candidate.getId() != null && best.getId() != null) 
                if (candidate.getId().compareTo(best.getId()) < 0) 
                    best = candidate;
        }
        return best;
    }

    private void addProcessSegment(List<GanttSegment> ganttChart, Process process, int startTime, int endTime) {
        if (startTime == endTime)
            return;
        if (!ganttChart.isEmpty()) {
            GanttSegment last = ganttChart.get(ganttChart.size() - 1);
            if (last.getType() == GanttSegment.SegmentType.PROCESS
                    && last.getProcessId().equals(process.getId())
                    && last.getEndTime() == startTime) {
                last.setEndTime(endTime);
                return;
            }
        }
        ganttChart.add(
            new GanttSegment(
                process.getId(),
                startTime,
                endTime,
                GanttSegment.SegmentType.PROCESS
            )
        );
    }

    private void addIdleSegment(List<GanttSegment> ganttChart, int startTime, int endTime) {
        if (startTime == endTime) {
            return;
        }
        if (!ganttChart.isEmpty()) {
            GanttSegment last = ganttChart.get(ganttChart.size() - 1);
            if (last.getType() == GanttSegment.SegmentType.IDLE
                    && last.getEndTime() == startTime) {
                last.setEndTime(endTime);
                return;
            }
        }
        ganttChart.add(
            new GanttSegment(
                null,
                startTime,
                endTime,
                GanttSegment.SegmentType.IDLE
            )
        );
    }

    private void addContextSwitch(List<GanttSegment> ganttChart, int currentTime, int contextSwitch) {
        if (contextSwitch <= 0) {
            return;
        }
        ganttChart.add(
            new GanttSegment(
                null,
                currentTime,
                currentTime + contextSwitch,
                GanttSegment.SegmentType.CONTEXT_SWITCH
            )
        );
    }
}