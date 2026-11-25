
package acs;

import java.util.List;
import model.Process;

public class ACSTier2{
    public Process selectHighestPriorityProcess(List<Process> readyProcesses) {
        if (readyProcesses == null || readyProcesses.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ready processes cannot be empty"
            );
        }
        Process best = readyProcesses.get(0);

        for (int i = 1; i < readyProcesses.size(); i++) {
            Process candidate = readyProcesses.get(i);

            if (candidate.getEffectivePriority() < best.getEffectivePriority()) {
                best = candidate;
            }
            else if (candidate.getEffectivePriority() == best.getEffectivePriority()) {
                if (candidate.getArrivalTime() < best.getArrivalTime()) {
                    best = candidate;
                }
                else if (candidate.getArrivalTime() == best.getArrivalTime()
                        && candidate.getId() != null
                        && best.getId() != null
                        && candidate.getId().compareTo(best.getId()) < 0) {
                    best = candidate;
                }
            }
        }

        return best;
    }

    public int executeNonPreemptive(Process process) {

        int executionDuration = process.getRemainingTime();

        process.setRemainingTime(0);

        return executionDuration;
    }
}
