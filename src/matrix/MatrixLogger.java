package matrix;
import model.GanttSegment;
import model.Process;
import scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class MatrixLogger {

    private final List<SchedulerSnapshot> snapshots;

    public MatrixLogger() {snapshots = new ArrayList<>();}
    public void logState(int currentTime, Process running, List<Process> tier1, List<Process> tier2, GanttSegment.SegmentType cpuState) {
        SchedulerSnapshot snapshot = new SchedulerSnapshot(running, currentTime, tier1, tier2, cpuState);
        snapshots.add(snapshot);
    }

    public List<SchedulerSnapshot> getSnapshots() {return Collections.unmodifiableList(new ArrayList<>(snapshots));}


    public void printMatrix() {

        System.out.println("\n========== ACS DEMOTION MATRIX ==========");

        System.out.printf(
                "%-6s | %-12s | %s%n",
                "Time", "Process", "Tier"
        );

        System.out.println("---------------------------------------------------------------");

        String previousProcess = "";

        for (SchedulerSnapshot snapshot : snapshots) {

            String processName = "";

            if (snapshot.getCpuState() == GanttSegment.SegmentType.PROCESS) {

                processName = snapshot.getRunningProcess().getProcessId();

            }

            String displayedProcess = processName;

            if (processName.equals(previousProcess)) {
                displayedProcess = "";
            }

            if (snapshot.getCpuState() == GanttSegment.SegmentType.CONTEXT_SWITCH) {
                displayedProcess = "";
            }

            if (snapshot.getCpuState() == GanttSegment.SegmentType.IDLE) {
                displayedProcess = "";
            }

            System.out.printf(
                    "%-6d | %-12s | ",
                    snapshot.getTimeStep(),
                    displayedProcess
            );

            if (snapshot.getCpuState() == GanttSegment.SegmentType.CONTEXT_SWITCH) {
                System.out.println("Context Switching");
            }
            else if (snapshot.getCpuState() == GanttSegment.SegmentType.IDLE) {
                System.out.println("CPU Idle");
            }
            else {
                System.out.println(
                        "Tier 1: " + formatProcesses(snapshot.getTier1())
                );
            }

            if (snapshot.getCpuState() != GanttSegment.SegmentType.CONTEXT_SWITCH
                    && snapshot.getCpuState() != GanttSegment.SegmentType.IDLE) {
                System.out.printf("%-6s | %-12s | Tier 2: %s%n",
                        "", "", formatProcesses(snapshot.getTier2()));
            }

            previousProcess = processName;
        }
    }


    private String formatProcesses(List<ProcessSnapshot> processes) {
        if (processes == null) throw new IllegalArgumentException("this process snapshot can't be null");
        if (processes.isEmpty()) return "Empty";
        StringBuilder result = new StringBuilder();
        for (ProcessSnapshot p: processes) {
            if (!result.isEmpty()) {
                result.append(", ");
            }
            result.append(p.getProcessId()).append("(R=").append(p.getRemainingTime()).append(", p=").append(p.getPriority()).append(", EP=").append(p.getEffectivePriority()).append(")");
        }
        return result.toString();
    }


}