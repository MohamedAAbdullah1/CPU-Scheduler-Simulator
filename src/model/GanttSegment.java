package model;

public class GanttSegment {

    private String processId;
    private int startTime;
    private int endTime;

    public enum SegmentType {
        PROCESS,
        CONTEXT_SWITCH,
        IDLE
    }

    private SegmentType type;

    public GanttSegment(String processId, int startTime,
                        int endTime, SegmentType type) {

        if (startTime < 0 || endTime < startTime) {
            throw new IllegalArgumentException("Invalid time range");
        }

        if (type == null) {
            throw new IllegalArgumentException("Segment type cannot be null");
        }

        if (type == SegmentType.PROCESS &&
                (processId == null || processId.isBlank())) {
            throw new IllegalArgumentException(
                    "Process ID is required for PROCESS segment"
            );
        }

        if (type != SegmentType.PROCESS) {
            processId = null;
        }

        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

    // getters
    public String getProcessId() {
        return processId;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public SegmentType getType() {
        return type;
    }

    // setters
    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    // Calculate segment duration
    public int getDuration() {
        return endTime - startTime;
    }

    @Override
    public String toString() {

        if (type == SegmentType.IDLE) {
            return "IDLE (" + startTime + " - " + endTime + ")";
        }

        if (type == SegmentType.CONTEXT_SWITCH) {
            return "CS (" + startTime + " - " + endTime + ")";
        }

        return processId + " (" + startTime + " - " + endTime + ")";
    }
}
