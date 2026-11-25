package acs;
import model.Process;
import model.GanttSegment;
import model.ScheduleResult;
import scheduler.Scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Comparator;

public class ACSTier1 implements Scheduler {
    private final int quantum;

    public ACSTier1(int quantum){
        if(quantum <= 0){
            throw new IllegalArgumentException(
                    "Quantum must be positive"
            );
        }
        this.quantum = quantum;
    }

    @Override
    public ScheduleResult run(List<Process> processes , int contextSwitch){
        ScheduleResult result = new ScheduleResult();

        if(processes == null || processes.isEmpty()){
            return result;
        }
        if(contextSwitch < 0){
            throw new IllegalArgumentException(
                    "Context switch cannot be negative"
            );
        }
        List<Process> sortedProcesses = new ArrayList<>(processes);
        sortedProcesses.sort(
                Comparator.comparingInt(p -> p.getArrivalTime())
        );

        Queue<Process> readyQueue = new LinkedList<>();
        List<GanttSegment> ganttChart = new ArrayList<>();
        int currentTime = 0;
        int nextArrival = 0;

        Process lastProcess = null;

        while (nextArrival < sortedProcesses.size() && sortedProcesses.get((nextArrival)).getArrivalTime() <= currentTime){
            readyQueue.add(sortedProcesses.get(nextArrival));
            nextArrival++;
        }
        while (!readyQueue.isEmpty() || nextArrival < sortedProcesses.size()){

            if(readyQueue.isEmpty()){
                int nextArrivalTime = sortedProcesses.get(nextArrival).getArrivalTime();
                addIdleSegment(ganttChart, currentTime, nextArrivalTime);
                currentTime = nextArrivalTime;
                lastProcess = null;

                while (nextArrival < sortedProcesses.size() && sortedProcesses.get(nextArrival).getArrivalTime() <= currentTime){
                    readyQueue.add(sortedProcesses.get(nextArrival));
                    nextArrival++;
                }
                continue;
            }
            Process currentProcess = readyQueue.poll();

            if(lastProcess != null && lastProcess != currentProcess){
                addContextSwitch(ganttChart , currentTime , contextSwitch);
                currentTime += contextSwitch;


                while (nextArrival < sortedProcesses.size() && sortedProcesses.get(nextArrival).getArrivalTime() <= currentTime){
                    readyQueue.add(sortedProcesses.get(nextArrival));
                    nextArrival++;
                }

            }

            if(currentProcess.getStartTime() == -1){
                currentProcess.setStartTime(currentTime);
            }
            int executationTime = Math.min(quantum , currentProcess.getRemainingTime());

            int executationStart = currentTime;
            int executationEnd = currentTime + executationTime;

            addProcessSegment(ganttChart , currentProcess , executationStart , executationEnd);

            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - executationTime);
            currentTime = executationEnd;

            while (nextArrival < sortedProcesses.size()
                    && sortedProcesses.get(nextArrival).getArrivalTime() <= currentTime) {

                readyQueue.add(sortedProcesses.get(nextArrival));
                nextArrival++;
            }


            if(currentProcess.isFinished()){
                currentProcess.setCompletionTime(currentTime);
            }
            else {
                currentProcess.enterTier2(currentTime);
                result.getDemotedProcesses().add(currentProcess);
            }
            lastProcess = currentProcess;

        }
        result.setGanttChart(ganttChart);
        result.setProcesses(processes);
        return result;
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
