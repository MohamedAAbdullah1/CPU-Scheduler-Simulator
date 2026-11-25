
package testing;

import acs.ACSTier1;
import model.Process;
import model.GanttSegment;
import model.ScheduleResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("       ACS TIER 1 TEST RUNNER");
        System.out.println("======================================");

        // Test 1: Process finishes before Quantum
        runTest(
                "Test 1 - Finish Before Quantum",
                3, 0,
                new Process[]{
                        new Process("P1", 0, 2, 2),
                        new Process("P2", 0, 1, 3)
                }
        );

        // Test 2: Process finishes exactly at Quantum
        runTest(
                "Test 2 - Exact Quantum",
                3, 0,
                new Process[]{
                        new Process("P1", 0, 3, 2),
                        new Process("P2", 0, 2, 3)
                }
        );

        // Test 3: Process gets demoted to Tier 2
        runTest(
                "Test 3 - Demotion",
                3, 0,
                new Process[]{
                        new Process("P1", 0, 5, 2),
                        new Process("P2", 1, 2, 3),
                        new Process("P3", 3, 1, 1)
                }
        );

        // Test 4: Initial Idle
        runTest(
                "Test 4 - Initial Idle",
                3, 0,
                new Process[]{
                        new Process("P1", 2, 1, 2),
                        new Process("P2", 2, 2, 3)
                }
        );

        // Test 5: Idle between processes
        runTest(
                "Test 5 - Idle Between Processes",
                3, 2,
                new Process[]{
                        new Process("P1", 0, 1, 2),
                        new Process("P2", 5, 2, 3)
                }
        );

        // Test 6: Context Switching
        runTest(
                "Test 6 - Context Switching",
                2, 2,
                new Process[]{
                        new Process("P1", 0, 2, 2),
                        new Process("P2", 1, 1, 3),
                        new Process("P3", 3, 1, 1)
                }
        );

        // Test 7: Multiple arrivals during Quantum
        runTest(
                "Test 7 - Multiple Arrivals During Quantum",
                4, 0,
                new Process[]{
                        new Process("P1", 0, 6, 2),
                        new Process("P2", 1, 2, 3),
                        new Process("P3", 2, 1, 1),
                        new Process("P4", 3, 2, 4)
                }
        );

        // Test 8: Context Switch = 0
        runTest(
                "Test 8 - Zero Context Switch",
                2, 0,
                new Process[]{
                        new Process("P1", 0, 2, 2),
                        new Process("P2", 0, 1, 3)
                }
        );

        // Test 9: Multiple processes with same arrival
        runTest(
                "Test 9 - Same Arrival Time",
                2, 0,
                new Process[]{
                        new Process("P1", 0, 1, 2),
                        new Process("P2", 0, 1, 3),
                        new Process("P3", 0, 2, 1)
                }
        );

        // Test 10: Empty input
        testEmptyInput();

        // Test 11: Null input
        testNullInput();

        // Test 12: Invalid Quantum
        testInvalidQuantum();

        // Test 13: Negative Context Switch
        testNegativeContextSwitch();

        // Final summary
        System.out.println("\n======================================");
        System.out.println("             TEST SUMMARY");
        System.out.println("======================================");

        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total : " + (passed + failed));

        if (failed == 0) {
            System.out.println("\nALL TESTS PASSED!");
        } else {
            System.out.println("\nSOME TESTS FAILED!");
        }
    }

    // Run a normal scheduling test
    private static void runTest(
            String testName,
            int quantum,
            int contextSwitch,
            Process[] processes) {

        System.out.println("\n--------------------------------------");
        System.out.println(testName);
        System.out.println("--------------------------------------");

        try {
            List<Process> processList =
                    new ArrayList<>(Arrays.asList(processes));

            ACSTier1 scheduler = new ACSTier1(quantum);

            ScheduleResult result =
                    scheduler.run(processList, contextSwitch);

            printResult(result);

            if (validateResult(result, processList)) {
                passed++;
                System.out.println("\nRESULT: PASS");
            } else {
                failed++;
                System.out.println("\nRESULT: FAIL");
            }

        } catch (Exception e) {
            failed++;
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("RESULT: FAIL");
        }
    }

    // Print the complete scheduling result
    private static void printResult(ScheduleResult result) {

        System.out.println("\nGANTT CHART:");

        for (GanttSegment segment : result.getGanttChart()) {

            System.out.println(
                    segment.getType()
                            + " | Process: " + segment.getProcessId()
                            + " | Start: " + segment.getStartTime()
                            + " | End: " + segment.getEndTime()
            );
        }

        System.out.println("\nPROCESS INFORMATION:");

        for (Process p : result.getProcesses()) {

            System.out.println(
                    p.getId()
                            + " | Arrival: " + p.getArrivalTime()
                            + " | Burst: " + p.getBurstTime()
                            + " | Start: " + p.getStartTime()
                            + " | Completion: " + p.getCompletionTime()
                            + " | Remaining: " + p.getRemainingTime()
                            + " | Tier: " + p.getTier()
            );
        }

        System.out.println("\nDEMOTED PROCESSES:");

        if (result.getDemotedProcesses().isEmpty()) {
            System.out.println("No processes were demoted.");
        } else {
            for (Process p : result.getDemotedProcesses()) {
                System.out.println(
                        p.getId()
                                + " | Remaining: " + p.getRemainingTime()
                                + " | Tier: " + p.getTier()
                );
            }
        }
    }

    // Basic validation
    private static boolean validateResult(
            ScheduleResult result,
            List<Process> processes) {

        if (result == null) {
            return false;
        }

        if (result.getGanttChart() == null
                || result.getProcesses() == null
                || result.getDemotedProcesses() == null) {
            return false;
        }

        // Check that no segment has zero or negative duration
        for (GanttSegment segment : result.getGanttChart()) {

            if (segment.getEndTime() <= segment.getStartTime()) {
                return false;
            }
        }

        // Check that all demoted processes are actually in Tier 2
        for (Process p : result.getDemotedProcesses()) {

            if (p.getTier() != 2 || p.getRemainingTime() <= 0) {
                return false;
            }
        }

        // Check that every process is either finished or demoted
        for (Process p : processes) {

            if (!p.isFinished() && p.getTier() != 2) {
                return false;
            }
        }

        return true;
    }

    // Test empty input
    private static void testEmptyInput() {

        System.out.println("\n--------------------------------------");
        System.out.println("Test 10 - Empty Input");
        System.out.println("--------------------------------------");

        try {
            ACSTier1 scheduler = new ACSTier1(3);

            ScheduleResult result =
                    scheduler.run(new ArrayList<>(), 0);

            boolean valid =
                    result != null
                            && result.getGanttChart().isEmpty()
                            && result.getProcesses().isEmpty();

            recordResult(valid);

        } catch (Exception e) {
            recordResult(false);
            System.out.println(e.getMessage());
        }
    }

    // Test null input
    private static void testNullInput() {

        System.out.println("\n--------------------------------------");
        System.out.println("Test 11 - Null Input");
        System.out.println("--------------------------------------");

        try {
            ACSTier1 scheduler = new ACSTier1(3);

            ScheduleResult result =
                    scheduler.run(null, 0);

            recordResult(
                    result != null
                            && result.getGanttChart().isEmpty()
            );

        } catch (Exception e) {
            recordResult(false);
            System.out.println(e.getMessage());
        }
    }

    // Test invalid Quantum
    private static void testInvalidQuantum() {

        System.out.println("\n--------------------------------------");
        System.out.println("Test 12 - Invalid Quantum");
        System.out.println("--------------------------------------");

        try {
            new ACSTier1(0);
            recordResult(false);

        } catch (IllegalArgumentException e) {
            recordResult(true);
        }
    }

    // Test negative Context Switch
    private static void testNegativeContextSwitch() {

        System.out.println("\n--------------------------------------");
        System.out.println("Test 13 - Negative Context Switch");
        System.out.println("--------------------------------------");

        try {
            ACSTier1 scheduler = new ACSTier1(3);

            scheduler.run(
                    new ArrayList<>(
                            List.of(new Process("P1", 0, 2, 1))
                    ),
                    -1
            );

            recordResult(false);

        } catch (IllegalArgumentException e) {
            recordResult(true);
        }
    }

    private static void recordResult(boolean valid) {

        if (valid) {
            passed++;
            System.out.println("RESULT: PASS");
        } else {
            failed++;
            System.out.println("RESULT: FAIL");
        }
    }
}
