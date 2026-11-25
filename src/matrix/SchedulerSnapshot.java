package matrix;

import model.Process;
import model.GanttSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SchedulerSnapshot {
    private final ProcessSnapshot runningProcess;
    private final int timeStep;

    private final List<ProcessSnapshot> tier1;
    private final List<ProcessSnapshot> tier2;

    private final GanttSegment.SegmentType cpuState;
    public SchedulerSnapshot(Process process, int currentTime, List<Process> tier1Processes, List<Process> tier2Processes, GanttSegment.SegmentType currentState) {
        if (currentState == null) throw new IllegalArgumentException("cpu state can't be null");
        if (currentState == GanttSegment.SegmentType.PROCESS) {
            if (process == null) throw new IllegalArgumentException("process can't be null when cpu is executing");
            this.runningProcess = new ProcessSnapshot(process);
        } else {
            if (process != null) throw new IllegalArgumentException("process must be null when cpu isn't executing");
            this.runningProcess = null;
        }
        this.timeStep = currentTime;
        this.cpuState = currentState;

        this.tier1 = createSnapshots(tier1Processes);
        this.tier2 = createSnapshots(tier2Processes);
    }

    private List<ProcessSnapshot> createSnapshots(List<Process> processes) {
        if (processes == null) throw new IllegalArgumentException("the list of processes can't be null");
        List<ProcessSnapshot> snapshot = new ArrayList<>();
        for (Process p: processes) {
            snapshot.add(new ProcessSnapshot(p));
        }
        return Collections.unmodifiableList(snapshot);
    }

    //getters
    public ProcessSnapshot getRunningProcess() {return runningProcess;}
    public int getTimeStep() {return timeStep;}
    public List<ProcessSnapshot> getTier1() {return tier1;}
    public List<ProcessSnapshot> getTier2() {return tier2;}
    public GanttSegment.SegmentType getCpuState() {return cpuState;}
}
