package scheduler;

import model.Process;
import model.ScheduleResult;
import java.util.List;

public interface Scheduler {

    ScheduleResult run(List<Process> processes, int contextSwitch);

}
