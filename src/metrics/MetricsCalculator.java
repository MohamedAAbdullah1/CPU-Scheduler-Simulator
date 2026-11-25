package metrics;
import model.Process;
import model.ScheduleResult;
import java.util.List;

public class MetricsCalculator {

    public int calculateWT(Process process) {
        validateCompletedProcess(process);
        return process.getWaitingTime();
    }

    public int calculateTAT(Process process) {
        validateCompletedProcess(process);
        return process.getTurnaroundTime();
    }

    public int calculateRT(Process process){
        validateStartedProcess(process);
        return process.getResponseTime();
    }

    public double calculateAverageWT(List<Process> processes){
        validateList(processes);
        if (processes.isEmpty()) return 0;
        double tot = 0; // we can change this to string message because the true mean for empty list is undefined.
        for (Process process:processes) tot += calculateWT(process);
        return tot /processes.size();
    }

    public double calculateAverageTAT(List<Process> processes) {
        validateList(processes);
        if(processes.isEmpty()) return 0; // we can change this to string message because the true mean for empty list is undefined.
        double tot = 0;
        for (Process process:processes) tot += calculateTAT(process);
        return tot / processes.size();
    }

    public double calculateAverageRT (List<Process> processes) {
        validateList(processes);
        if(processes.isEmpty()) return 0;
        double tot = 0;
        for (Process process: processes) tot += calculateRT(process);
        return tot / processes.size();
    }

    public void calculateMetrics(ScheduleResult scheduleResult) {
        validateScheduleResult(scheduleResult);
        double averageWT, averageRT, averageTAT;
        List<Process> processes = scheduleResult.getProcesses();
        averageWT = calculateAverageWT(processes);
        averageRT = calculateAverageRT(processes);
        averageTAT = calculateAverageTAT(processes);

        scheduleResult.setAverageWaitingTime(averageWT);
        scheduleResult.setAverageResponseTime(averageRT);
        scheduleResult.setAverageTurnaroundTime(averageTAT);
    }

    private void validateCompletedProcess(Process process) {
        if (process == null) {
            throw new IllegalArgumentException("Process can't be null!"); // Argument because it is invalid type null not process.
        }
        else if(!process.isFinished()|| process.getCompletionTime() == -1) {
            throw new IllegalStateException("Process has not finished or the completion time still -1."); // state because the process doesn't finished yet.
        }
    }
    private void validateStartedProcess(Process process) {
        if (process == null) {
            throw new IllegalArgumentException("Process can't be null!");
        }
        else if(process.getStartTime() == -1) {
            throw new IllegalStateException("Process doesn't started yet.");
        }
    }
    private void validateList(List<Process> processes) {
        if (processes == null)
            throw new IllegalArgumentException("List can't be null!");
    }
    private void validateScheduleResult(ScheduleResult scheduleResult) {
        if (scheduleResult == null)
            throw new IllegalArgumentException("Schedule result can't be null!");
    }
}
