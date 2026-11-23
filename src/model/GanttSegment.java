package model;

public class GanttSegment {

    private String processId;
    private int startTime;
    private int endTime;
    private SegmentType type;
        public enum SegmentType {
        PROCESS,
        CONTEXT_SWITCH,
        IDLE
    }

    public GanttSegment(String processId, int startTime, int endTime, SegmentType type) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

    // getters

    public String getProcessId() {return processId;}
    public int getStartTime() {return startTime;}
    public int getEndTime() {return endTime;}
    public SegmentType getType() {return type;}

    // setters

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
    // Calculate segment duration
    public int getDuration() {
        return endTime - startTime;
    }

    // @Override
    // public String toString() {
    //     if (type == SegmentType.IDLE) {
    //         return "IDLE (" + startTime + " - " + endTime + ")";
    //     }

    //     if (type == SegmentType.CONTEXT_SWITCH) {
    //         return "CS (" + startTime + " - " + endTime + ")";
    //     }

    //     return processId + " (" + startTime + " - " + endTime + ")";
    // }
}
