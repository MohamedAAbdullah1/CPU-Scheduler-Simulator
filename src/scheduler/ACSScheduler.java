package scheduler;

import acs.ACSTier1;
import acs.ACSTier2;
import acs.AgingManager;

import model.Process;
import model.GanttSegment;
import model.ScheduleResult;

import java.util.*;

public class ACSScheduler implements Scheduler {
    private final int quantum;
    private final int agingInterval;

    private final ACSTier1 tier1;
    private final ACSTier2 tier2;
    private final AgingManager agingManager;

    public ACSScheduler(int quantum , int agingInterval){
        if(quantum <= 0 || agingInterval <=0){
            throw new IllegalArgumentException(
                    "Quantum and Aging Interval must be positive"
            );
        }
        this.quantum = quantum;
        this.agingInterval = agingInterval;

        this.tier1 = new ACSTier1(quantum);
        this.tier2 = new ACSTier2();
        this.agingManager = new AgingManager(agingInterval);
    }

    @Override
    public ScheduleResult run(List<Process> processes, int contextSwitch) {
        ScheduleResult result = new ScheduleResult();

        if (processes == null || processes.isEmpty()) {
            return result;
        }
        if (contextSwitch < 0) {
            throw new IllegalArgumentException(
                    "Context switch cannot be negative"
            );
        }

        List<Process> sortedProcesses = new ArrayList<>(processes);
        sortedProcesses.sort(
                Comparator.comparingInt(Process::getArrivalTime)
                        .thenComparing(Process::getId)
        );

        Queue<Process> tier1Ready = new LinkedList<>();
        List<Process> tier2Ready = new ArrayList<>();
        List<GanttSegment> ganttChart = new ArrayList<>();
        List<Process> demotedProcesses = new ArrayList<>();

        int currentTime = 0;
        int nextArrival = 0;

        Process lastProcess = null;

        while (hasUnFinishedProcess(sortedProcesses)) {
            nextArrival = addArrivedProcesses(sortedProcesses, tier1Ready, nextArrival, currentTime);

            if (tier1Ready.isEmpty() && tier2Ready.isEmpty()) {
                if (nextArrival < sortedProcesses.size()) {
                    int nextTimeArrival = sortedProcesses.get(nextArrival).getArrivalTime();

                    addIdleSegment(ganttChart, currentTime, nextTimeArrival);

                    currentTime = nextTimeArrival;
                    lastProcess = null;
                    continue;
                }
            }



            Process currentProcess;
            if (!tier1Ready.isEmpty()) {
                currentProcess = tier1Ready.poll();
                if (lastProcess != null && lastProcess != currentProcess) {
                    addContextSwitch(ganttChart, currentTime, contextSwitch);
                    currentTime += contextSwitch;
                    nextArrival = addArrivedProcesses(sortedProcesses, tier1Ready, nextArrival, currentTime);
                }


                if (currentProcess.getStartTime() == -1) {
                    currentProcess.setStartTime(currentTime);
                }
                int executionTime = tier1.executeQuantum(currentProcess);
                int endTime = currentTime + executionTime;

                addProcessSegment(ganttChart, currentProcess, currentTime, endTime);
                currentTime = endTime;

                nextArrival = addArrivedProcesses(sortedProcesses, tier1Ready, nextArrival, currentTime);

                if (currentProcess.isFinished()) {
                    currentProcess.setCompletionTime(currentTime);
                } else {
                    currentProcess.enterTier2(currentTime);
                    tier2Ready.add(currentProcess);
                    demotedProcesses.add(currentProcess);
                }

            }



            else {
                agingManager.applyAging(tier2Ready, currentTime);

                currentProcess =
                        tier2.selectHighestPriorityProcess(tier2Ready);

                tier2Ready.remove(currentProcess);

                if (lastProcess != null && lastProcess != currentProcess) {

                    addContextSwitch(ganttChart, currentTime, contextSwitch);
                    currentTime += contextSwitch;

                    nextArrival = addArrivedProcesses(
                            sortedProcesses,
                            tier1Ready,
                            nextArrival,
                            currentTime
                    );

                    // Tier 1 has higher priority
                    if (!tier1Ready.isEmpty()) {
                        tier2Ready.add(currentProcess);
                        lastProcess = null;
                        continue;
                    }
                }

                // Reconsider Tier 2 priorities after context switch
                tier2Ready.add(currentProcess);

                agingManager.applyAging(tier2Ready, currentTime);

                currentProcess =
                        tier2.selectHighestPriorityProcess(tier2Ready);

                tier2Ready.remove(currentProcess);

                if (currentProcess.getStartTime() == -1) {
                    currentProcess.setStartTime(currentTime);
                }

                int executionTime =
                        tier2.executeNonPreemptive(currentProcess);

                int endTime = currentTime + executionTime;

                addProcessSegment(
                        ganttChart,
                        currentProcess,
                        currentTime,
                        endTime
                );

                currentTime = endTime;

                currentProcess.setCompletionTime(currentTime);

                nextArrival = addArrivedProcesses(
                        sortedProcesses,
                        tier1Ready,
                        nextArrival,
                        currentTime
                );
            }
            lastProcess = currentProcess;
        }

        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        double totalResponseTime = 0;

        for (Process process : sortedProcesses) {
            totalWaitingTime += process.getWaitingTime();
            totalTurnaroundTime += process.getTurnaroundTime();
            totalResponseTime += process.getResponseTime();
        }

        int numberOfProcesses = sortedProcesses.size();

        result.setAverageWaitingTime(
                totalWaitingTime / numberOfProcesses
        );

        result.setAverageTurnaroundTime(
                totalTurnaroundTime / numberOfProcesses
        );

        result.setAverageResponseTime(
                totalResponseTime / numberOfProcesses
        );

        result.setGanttChart(ganttChart);
        result.setProcesses(sortedProcesses);
        result.setDemotedProcesses(demotedProcesses);

        return result;
    }


    private int addArrivedProcesses(List<Process> sortedProcesses , Queue<Process> tier1Queue , int nextArrival , int currentTime){
        while (nextArrival < sortedProcesses.size() && sortedProcesses.get(nextArrival).getArrivalTime() <= currentTime){
            tier1Queue.add(sortedProcesses.get(nextArrival));
            nextArrival++;
        }
        return nextArrival;
    }
    private boolean hasUnFinishedProcess(List<Process> processes){
        for (Process process : processes){
            if(process.getRemainingTime() > 0){
                return true;
            }
        }
        return false;
    }

    private void addProcessSegment(List<GanttSegment> ganttChart , Process process ,int startTime , int endTime ){
        if(startTime == endTime){
            return;
        }
        if (!ganttChart.isEmpty()) {

            GanttSegment last =
                    ganttChart.get(ganttChart.size() - 1);

            if (last.getType()
                    == GanttSegment.SegmentType.PROCESS
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
    private void addIdleSegment(List<GanttSegment> ganttChart , int startTime , int endTime){
        if(startTime == endTime){
            return;
        }
        if(!ganttChart.isEmpty()){
            GanttSegment last = ganttChart.get(ganttChart.size() - 1);

            if(
                    last.getType() == GanttSegment.SegmentType.IDLE
                            && last.getEndTime() == startTime
            ){
                last.setEndTime(endTime);
                return;
            }
        }
        ganttChart.add(
                new GanttSegment(
                        null ,
                        startTime,
                        endTime,
                        GanttSegment.SegmentType.IDLE
                )
        );

    }

    private void addContextSwitch(List<GanttSegment> ganttChart , int currentTime , int contextSwitch){
        if(contextSwitch <=0){
            return;
        }
        ganttChart.add(new GanttSegment(null , currentTime , currentTime + contextSwitch , GanttSegment.SegmentType.CONTEXT_SWITCH));
    }

}
