package acs;

import java.util.List;
import model.Process;

public class AgingManager {

    private final int agingInterval;

    public AgingManager(int agingInterval) {
        this.agingInterval = Math.max(1, agingInterval); // to prevent 0 and negative values
    }

    public void applyAging(List<Process> waitingProcesses, int currentTime) {
        if (waitingProcesses == null)
            return;
        for (Process process : waitingProcesses) {
            updateEffectivePriority(process, currentTime);
        }
    }

    public void updateEffectivePriority(Process process, int currentTime) {
        if (process == null || process.isFinished())
            return;
        if (process.getTier2EntryTime() < 0 || currentTime < process.getTier2EntryTime())
            return;
        process.updateEffectivePriority(currentTime, agingInterval);
    }

}