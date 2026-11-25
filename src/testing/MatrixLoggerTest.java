
package testing;

import matrix.MatrixLogger;
import matrix.SchedulerSnapshot;
import model.Process;
import model.GanttSegment;

import java.util.ArrayList;
import java.util.List;

public class MatrixLoggerTest {

    public static void main(String[] args) {

        // 1. Create Processes
        Process p1 = new Process("P1", 0, 5, 2);
        Process p2 = new Process("P2", 1, 3, 3);
        Process p3 = new Process("P3", 2, 2, 1);

        // 2. Create Tiers
        List<Process> tier1 = new ArrayList<>();
        tier1.add(p2);

        List<Process> tier2 = new ArrayList<>();
        tier2.add(p3);

        // 3. Create MatrixLogger
        MatrixLogger logger = new MatrixLogger();

        // 4. Record first snapshot at T=0
        logger.logState(
                0, p1, tier1, tier2,
                GanttSegment.SegmentType.PROCESS
        );

        // 5. Execute processes to change their data
        p1.execute();
        p2.execute();

        // 6. Record second snapshot at T=1
        logger.logState(
                1, p1, tier1, tier2,
                GanttSegment.SegmentType.PROCESS
        );

        // 7. Test Context Switching
        logger.logState(
                2, null, tier1, tier2,
                GanttSegment.SegmentType.CONTEXT_SWITCH
        );

        // 8. Test CPU Idle
        logger.logState(
                3, null,
                new ArrayList<>(),
                new ArrayList<>(),
                GanttSegment.SegmentType.IDLE
        );

        // 9. Print Matrix
        logger.printMatrix();

        // 10. Validate snapshots
        List<SchedulerSnapshot> snapshots = logger.getSnapshots();

        check(snapshots.size() == 4,
                "Snapshot count");

        check(snapshots.get(0)
                        .getRunningProcess()
                        .getRemainingTime() == 5,
                "Original snapshot data");

        check(snapshots.get(1)
                        .getRunningProcess()
                        .getRemainingTime() == 4,
                "Updated snapshot data");

        check(snapshots.get(0)
                        .getTier1().get(0)
                        .getRemainingTime() == 3,
                "Tier 1 snapshot data");

        check(snapshots.get(1)
                        .getTier1().get(0)
                        .getRemainingTime() == 2,
                "Tier 1 updated snapshot");

        check(snapshots.get(2).getCpuState()
                        == GanttSegment.SegmentType.CONTEXT_SWITCH,
                "Context switch state");

        check(snapshots.get(3).getCpuState()
                        == GanttSegment.SegmentType.IDLE,
                "CPU idle state");

        check(snapshots.get(3).getTier1().isEmpty(),
                "Empty Tier 1");

        check(snapshots.get(3).getTier2().isEmpty(),
                "Empty Tier 2");

        System.out.println("\nAll MatrixLogger tests passed!");
    }

    private static void check(boolean condition, String testName) {

        if (!condition) {
            throw new AssertionError(testName + " FAILED");
        }

        System.out.println(testName + ": PASSED");
    }
}
