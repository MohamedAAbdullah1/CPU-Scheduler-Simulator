
package testing;

import scheduler.ACSScheduler;
import model.Process;
import model.GanttSegment;
import model.ScheduleResult;
import metrics.MetricsCalculator;

import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    private static final int AGING_INTERVAL = 4;

    public static void main(String[] args) {

        // Assignment Test Case 1
        runTest(
                "TEST CASE 1",
                3,  // Quantum
                0,  // Context Switching
                new Process("P1", 0, 4, 2),
                new Process("P2", 1, 7, 3),
                new Process("P3", 3, 2, 1)
        );

        // Assignment Test Case 2
        runTest(
                "TEST CASE 2",
                3,  // Quantum
                1,  // Context Switching
                new Process("P1", 0, 5, 4),
                new Process("P2", 1, 6, 2),
                new Process("P3", 4, 4, 3)
        );
    }

    // Run one complete ACS test case
    private static void runTest(
            String testName,
            int quantum,
            int contextSwitch,
            Process... templates
    ) {

        System.out.println("\n============================================");
        System.out.println("              " + testName);
        System.out.println("============================================");

        System.out.println("Quantum: " + quantum);
        System.out.println("Context Switching: " + contextSwitch);
        System.out.println("Aging Interval: " + AGING_INTERVAL);

        // Create fresh processes
        List<Process> processes = copyProcesses(templates);

        // Create ACS scheduler
        ACSScheduler scheduler =
                new ACSScheduler(quantum, AGING_INTERVAL);

        // Execute simulation
        ScheduleResult result =
                scheduler.run(processes, contextSwitch);

        if (result == null) {
            throw new IllegalStateException(
                    "Scheduler returned null result"
            );
        }

        // Print time-step timeline
        printTimeSteps(result);

        // Print process analysis
        printProcessAnalysis(result);

        System.out.println("\n========== TEST COMPLETED ==========");
    }

    // Create independent processes for each test
    private static List<Process> copyProcesses(Process[] templates) {

        List<Process> processes = new ArrayList<>();

        for (Process p : templates) {

            Process copy = new Process(
                    p.getId(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getPriority()
            );

            processes.add(copy);
        }

        return processes;
    }

    // Print one row for every time step
    private static void printTimeSteps(ScheduleResult result) {

        System.out.println("\n========== TIME STEP MATRIX ==========");

        System.out.printf(
                "%-8s | %-20s%n",
                "Time",
                "Process"
        );

        System.out.println("--------------------------------------");

        List<GanttSegment> ganttChart = result.getGanttChart();

        if (ganttChart == null || ganttChart.isEmpty()) {
            System.out.println("No execution data.");
            return;
        }

        // Find simulation finish time
        int finishTime = 0;

        for (GanttSegment segment : ganttChart) {
            finishTime = Math.max(
                    finishTime,
                    segment.getEndTime()
            );
        }

        // Print every time unit
        for (int time = 0; time < finishTime; time++) {

            String processName = "UNRECORDED";

            for (GanttSegment segment : ganttChart) {

                if (time >= segment.getStartTime()
                        && time < segment.getEndTime()) {

                    if (segment.getType()
                            == GanttSegment.SegmentType.PROCESS) {

                        processName = segment.getProcessId();

                    } else if (segment.getType()
                            == GanttSegment.SegmentType.CONTEXT_SWITCH) {

                        processName = "CONTEXT SWITCH";

                    } else {

                        processName = "IDLE";
                    }

                    break;
                }
            }

            System.out.printf(
                    "%-8d | %-20s%n",
                    time,
                    processName
            );
        }

        System.out.println("--------------------------------------");
        System.out.println("Finish Time: " + finishTime);
    }

    // Print individual and average metrics
    private static void printProcessAnalysis(ScheduleResult result) {

        MetricsCalculator calculator =
                new MetricsCalculator();

        List<Process> processes = result.getProcesses();

        System.out.println("\n========== PROCESS ANALYSIS ==========");

        System.out.printf(
                "%-8s | %-8s | %-8s | %-8s | %-8s | %-8s%n",
                "Process",
                "Arrival",
                "Finish",
                "TAT",
                "WT",
                "RT"
        );

        System.out.println(
                "---------------------------------------------------------------"
        );

        for (Process p : processes) {

            int finishTime =
                    p.getArrivalTime() + calculator.calculateTAT(p);

            System.out.printf(
                    "%-8s | %-8d | %-8d | %-8d | %-8d | %-8d%n",
                    p.getId(),
                    p.getArrivalTime(),
                    finishTime,
                    calculator.calculateTAT(p),
                    calculator.calculateWT(p),
                    calculator.calculateRT(p)
            );
        }

        // Calculate averages independently
        double avgWT = calculator.calculateAverageWT(processes);
        double avgTAT = calculator.calculateAverageTAT(processes);
        double avgRT = calculator.calculateAverageRT(processes);

        System.out.println(
                "\nAverage Waiting Time: " + String.format("%.2f", avgWT)
        );

        System.out.println(
                "Average Turnaround Time: " + String.format("%.2f", avgTAT)
        );

        System.out.println(
                "Average Response Time: " + String.format("%.2f", avgRT)
        );

        // Compare calculated metrics with scheduler results
        double epsilon = 0.000001;

        boolean valid =
                Math.abs(result.getAverageWaitingTime() - avgWT) < epsilon
                        && Math.abs(result.getAverageTurnaroundTime() - avgTAT) < epsilon
                        && Math.abs(result.getAverageResponseTime() - avgRT) < epsilon;

        System.out.println(
                "\nMetrics Validation: " +
                        (valid ? "PASS" : "FAIL")
        );
    }
}
