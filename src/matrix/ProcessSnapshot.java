package matrix;
import model.Process;

public class ProcessSnapshot {
    private final String processId;
    private final int remainingTime, priority, effectivePriority;

    public ProcessSnapshot(Process process) {
        validateProcess(process);
        this.processId = process.getId();
        this.remainingTime = process.getRemainingTime();
        this.priority = process.getPriority();
        this.effectivePriority = process.getEffectivePriority();
    }

    private void validateProcess(Process process) {
        if (process == null)
            throw new IllegalArgumentException("The process can't be null");
    }

    //getters
    public String getProcessId() {return processId;}
    public int getRemainingTime() {return remainingTime;}
    public int getPriority() {return priority;}
    public int getEffectivePriority() {return effectivePriority;}
}
