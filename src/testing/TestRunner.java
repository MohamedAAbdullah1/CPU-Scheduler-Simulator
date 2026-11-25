
package testing;

import scheduler.ACSScheduler;

import model.Process;
import model.GanttSegment;
import model.ScheduleResult;

import java.util.*;

public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("          ACS TEST RUNNER");
        System.out.println("======================================");

        runTestCase1();
        runTestCase2();

        runTest3_ArrivalDuringTier2();
        runTest4_ArrivalDuringContextSwitch();
        runTest5_SameEffectivePriority();
        runTest6_IdleTime();

        // Final Summary
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

    // ==========================================
    // TEST CASE 1
    // ==========================================

    private static void runTestCase1() {

        List<Process> processes = new ArrayList<>();

        processes.add(new Process("P1", 0, 4, 2));
        processes.add(new Process("P2", 1, 7, 3));
        processes.add(new Process("P3", 3, 2, 1));

        // Q = 3, CS = 0, Aging Interval = 4
        ACSScheduler scheduler = new ACSScheduler(3, 4);

        ScheduleResult result = scheduler.run(processes, 0);

        System.out.println("\n========== TEST CASE 1 ==========");

        printResult(result);

        String[] types = {
                "PROCESS",
                "PROCESS",
                "PROCESS",
                "PROCESS",
                "PROCESS"
        };

        String[] ids = {
                "P1", "P2", "P3", "P1", "P2"
        };

        int[] starts = {0, 3, 6, 8, 9};
        int[] ends = {3, 6, 8, 9, 13};

        boolean valid = validateExpectedGantt(
                result, types, ids, starts, ends
        );

        valid &= validateCompletionTimes(
                result,
                Map.of("P1", 9, "P2", 13, "P3", 8)
        );

        valid &= validateMetrics(
                result, 4.333333, 8.666667, 1.666667
        );

        recordResult("Test Case 1", valid);
    }

    // ==========================================
    // TEST CASE 2
    // ==========================================

    private static void runTestCase2() {

        List<Process> processes = new ArrayList<>();

        processes.add(new Process("P1", 0, 5, 4));
        processes.add(new Process("P2", 1, 6, 2));
        processes.add(new Process("P3", 4, 4, 3));

        // Q = 3, CS = 1, Aging Interval = 4
        ACSScheduler scheduler = new ACSScheduler(3, 4);

        ScheduleResult result = scheduler.run(processes, 1);

        System.out.println("\n========== TEST CASE 2 ==========");

        printResult(result);

        String[] types = {
                "PROCESS",
                "CONTEXT_SWITCH",
                "PROCESS",
                "CONTEXT_SWITCH",
                "PROCESS",
                "CONTEXT_SWITCH",
                "PROCESS",
                "CONTEXT_SWITCH",
                "PROCESS",
                "CONTEXT_SWITCH",
                "PROCESS"
        };

        String[] ids = {
                "P1",
                null,
                "P2",
                null,
                "P3",
                null,
                "P2",
                null,
                "P1",
                null,
                "P3"
        };

        int[] starts = {
                0, 3, 4, 7, 8, 11, 12, 15, 16, 18, 19
        };

        int[] ends = {
                3, 4, 7, 8, 11, 12, 15, 16, 18, 19, 20
        };

        boolean valid = validateExpectedGantt(
                result, types, ids, starts, ends
        );

        valid &= validateCompletionTimes(
                result,
                Map.of("P1", 18, "P2", 15, "P3", 20)
        );

        valid &= validateMetrics(
                result, 11.0, 16.0, 2.333333
        );

        recordResult("Test Case 2", valid);
    }

    // ==========================================
    // TEST 3: ARRIVAL DURING TIER 2
    // ==========================================

    private static void runTest3_ArrivalDuringTier2() {

        List<Process> processes = new ArrayList<>();

        processes.add(new Process("P1", 0, 4, 2));
        processes.add(new Process("P2", 3, 1, 1));

        // Q = 2, CS = 0
        ACSScheduler scheduler = new ACSScheduler(2, 4);

        ScheduleResult result = scheduler.run(processes, 0);

        System.out.println(
                "\n========== TEST 3: ARRIVAL DURING TIER 2 =========="
        );

        printResult(result);

        String[] types = {
                "PROCESS",
                "PROCESS"
        };

        String[] ids = {
                "P1",
                "P2"
        };

        int[] starts = {0, 4};
        int[] ends = {4, 5};

        boolean valid = validateExpectedGantt(
                result, types, ids, starts, ends
        );

        valid &= validateCompletionTimes(
                result,
                Map.of("P1", 4, "P2", 5)
        );

        recordResult("Test 3 - Arrival During Tier 2", valid);
    }

    // ==========================================
    // TEST 4: ARRIVAL DURING CONTEXT SWITCH
    // ==========================================

    private static void runTest4_ArrivalDuringContextSwitch() {

        List<Process> processes = new ArrayList<>();

        processes.add(new Process("P1", 0, 4, 2));
        processes.add(new Process("P2", 0, 2, 3));
        processes.add(new Process("P3", 7, 1, 1));

        // Q = 2, CS = 2
        ACSScheduler scheduler = new ACSScheduler(2, 4);

        ScheduleResult result = scheduler.run(processes, 2);

        System.out.println(
                "\n========== TEST 4: ARRIVAL DURING CONTEXT SWITCH =========="
        );

        printResult(result);

        String[] types = {
                "PROCESS",
                "CONTEXT_SWITCH",
                "PROCESS",
                "CONTEXT_SWITCH",
                "PROCESS",
                "CONTEXT_SWITCH",
                "PROCESS"
        };

        String[] ids = {
                "P1",
                null,
                "P2",
                null,
                "P3",
                null,
                "P1"
        };

        int[] starts = {
                0, 2, 4, 6, 8, 9, 11
        };

        int[] ends = {
                2, 4, 6, 8, 9, 11, 13
        };

        boolean valid = validateExpectedGantt(
                result, types, ids, starts, ends
        );

        valid &= validateCompletionTimes(
                result,
                Map.of("P1", 13, "P2", 6, "P3", 9)
        );

        recordResult("Test 4 - Arrival During Context Switch", valid);
    }

    // ==========================================
    // TEST 5: SAME EFFECTIVE PRIORITY
    // ==========================================

    private static void runTest5_SameEffectivePriority() {

        List<Process> processes = new ArrayList<>();

        processes.add(new Process("P1", 0, 4, 2));
        processes.add(new Process("P2", 0, 4, 2));

        // Q = 2, CS = 0, Aging Interval = 100
        ACSScheduler scheduler = new ACSScheduler(2, 100);

        ScheduleResult result = scheduler.run(processes, 0);

        System.out.println(
                "\n========== TEST 5: SAME EFFECTIVE PRIORITY =========="
        );

        printResult(result);

        String[] types = {
                "PROCESS",
                "PROCESS",
                "PROCESS",
                "PROCESS"
        };

        String[] ids = {
                "P1",
                "P2",
                "P1",
                "P2"
        };

        int[] starts = {0, 2, 4, 6};
        int[] ends = {2, 4, 6, 8};

        boolean valid = validateExpectedGantt(
                result, types, ids, starts, ends
        );

        valid &= validateCompletionTimes(
                result,
                Map.of("P1", 6, "P2", 8)
        );

        recordResult("Test 5 - Same Effective Priority", valid);
    }

    // ==========================================
    // TEST 6: IDLE TIME
    // ==========================================

    private static void runTest6_IdleTime() {

        List<Process> processes = new ArrayList<>();

        processes.add(new Process("P1", 3, 1, 2));
        processes.add(new Process("P2", 5, 1, 3));

        // Q = 2, CS = 1
        ACSScheduler scheduler = new ACSScheduler(2, 4);

        ScheduleResult result = scheduler.run(processes, 1);

        System.out.println(
                "\n========== TEST 6: IDLE TIME =========="
        );

        printResult(result);

        String[] types = {
                "IDLE",
                "PROCESS",
                "IDLE",
                "PROCESS"
        };

        String[] ids = {
                null,
                "P1",
                null,
                "P2"
        };

        int[] starts = {0, 3, 4, 5};
        int[] ends = {3, 4, 5, 6};

        boolean valid = validateExpectedGantt(
                result, types, ids, starts, ends
        );

        valid &= validateCompletionTimes(
                result,
                Map.of("P1", 4, "P2", 6)
        );

        recordResult("Test 6 - Idle Time", valid);
    }

    // ==========================================
    // PRINT RESULT
    // ==========================================

    private static void printResult(ScheduleResult result) {

        if (result == null) {
            System.out.println("Result is NULL!");
            return;
        }

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
                            + " | WT: " + p.getWaitingTime()
                            + " | TAT: " + p.getTurnaroundTime()
                            + " | RT: " + p.getResponseTime()
            );
        }

        System.out.println("\nDEMOTED PROCESSES:");

        for (Process p : result.getDemotedProcesses()) {
            System.out.println(p.getId());
        }

        System.out.println("\nAVERAGE METRICS:");

        System.out.println(
                "Average Waiting Time: "
                        + result.getAverageWaitingTime()
        );

        System.out.println(
                "Average Turnaround Time: "
                        + result.getAverageTurnaroundTime()
        );

        System.out.println(
                "Average Response Time: "
                        + result.getAverageResponseTime()
        );
    }

    // ==========================================
    // VALIDATE GANTT CHART
    // ==========================================

    private static boolean validateExpectedGantt(
            ScheduleResult result,
            String[] expectedTypes,
            String[] expectedIds,
            int[] expectedStarts,
            int[] expectedEnds) {

        if (result == null || result.getGanttChart() == null) {
            System.out.println("Invalid ScheduleResult!");
            return false;
        }

        List<GanttSegment> actual = result.getGanttChart();

        if (actual.size() != expectedTypes.length) {

            System.out.println(
                    "Gantt segment count mismatch!"
                            + " Expected: " + expectedTypes.length
                            + " Actual: " + actual.size()
            );

            return false;
        }

        for (int i = 0; i < actual.size(); i++) {

            GanttSegment segment = actual.get(i);

            boolean correctType =
                    expectedTypes[i].equals(
                            segment.getType().toString()
                    );

            boolean correctId =
                    Objects.equals(
                            expectedIds[i],
                            segment.getProcessId()
                    );

            boolean correctStart =
                    expectedStarts[i] == segment.getStartTime();

            boolean correctEnd =
                    expectedEnds[i] == segment.getEndTime();

            if (!correctType || !correctId
                    || !correctStart || !correctEnd) {

                System.out.println("\nMismatch at segment " + i);

                System.out.println(
                        "Expected: "
                                + expectedTypes[i]
                                + " | " + expectedIds[i]
                                + " | " + expectedStarts[i]
                                + "-" + expectedEnds[i]
                );

                System.out.println(
                        "Actual: "
                                + segment.getType()
                                + " | " + segment.getProcessId()
                                + " | " + segment.getStartTime()
                                + "-" + segment.getEndTime()
                );

                return false;
            }
        }

        return true;
    }

    // ==========================================
    // VALIDATE COMPLETION TIMES
    // ==========================================

    private static boolean validateCompletionTimes(
            ScheduleResult result,
            Map<String, Integer> expectedCompletion) {

        if (result == null || result.getProcesses() == null) {
            return false;
        }

        if (result.getProcesses().size() != expectedCompletion.size()) {
            System.out.println("Process count mismatch!");
            return false;
        }

        for (Process p : result.getProcesses()) {

            Integer expectedTime =
                    expectedCompletion.get(p.getId());

            if (expectedTime == null
                    || p.getCompletionTime() != expectedTime) {

                System.out.println(
                        "Wrong completion time for " + p.getId()
                                + " | Expected: " + expectedTime
                                + " | Actual: " + p.getCompletionTime()
                );

                return false;
            }
        }

        return true;
    }

    // ==========================================
    // VALIDATE AVERAGE METRICS
    // ==========================================

    private static boolean validateMetrics(
            ScheduleResult result,
            double expectedWT,
            double expectedTAT,
            double expectedRT) {

        if (result == null) {
            return false;
        }

        boolean wtCorrect = closeEnough(
                result.getAverageWaitingTime(), expectedWT
        );

        boolean tatCorrect = closeEnough(
                result.getAverageTurnaroundTime(), expectedTAT
        );

        boolean rtCorrect = closeEnough(
                result.getAverageResponseTime(), expectedRT
        );

        if (!wtCorrect) {
            System.out.println("Average WT mismatch!");
        }

        if (!tatCorrect) {
            System.out.println("Average TAT mismatch!");
        }

        if (!rtCorrect) {
            System.out.println("Average RT mismatch!");
        }

        return wtCorrect && tatCorrect && rtCorrect;
    }

    // ==========================================
    // DOUBLE COMPARISON
    // ==========================================

    private static boolean closeEnough(
            double actual,
            double expected) {

        return Math.abs(actual - expected) < 0.01;
    }

    // ==========================================
    // RECORD TEST RESULT
    // ==========================================

    private static void recordResult(
            String name,
            boolean valid) {

        if (valid) {
            passed++;
            System.out.println("\n" + name + ": PASS");
        } else {
            failed++;
            System.out.println("\n" + name + ": FAIL");
        }
    }
}
