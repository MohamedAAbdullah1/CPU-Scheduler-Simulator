package model;

public class Process {
    private String id;
    private int arrivalTime;  // time process arrive 
    private int burstTime;      // needed time in cpu
    private int remainingTime;  
    private int priority;
    private int startTime = -1;   // start time in CPU
    private int completionTime = -1;      // finish time

    // attributes for ACS
    private int effectivePriority;
    private int tier2EntryTime = -1;    // when enter tier 2
    private int tier = 1;

    public Process(String id , int arrivalTime , int burstTime , int priority){
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;   // total time required in CPU
        this.remainingTime = burstTime;   // at first it required burst time
        this.priority = priority;
        this.effectivePriority = priority;
    }


    // getters 
    public String getId(){return id;}
    public int getArrivalTime(){return arrivalTime;}
    public int getBurstTime(){return burstTime;}
    public int getRemainingTime(){return remainingTime;}
    public int getPriority(){return priority;}
    public int getStartTime(){return startTime;}
    public int getCompletionTime(){return completionTime;}
    public int getEffectivePriority(){return effectivePriority;}
    public int getTier2EntryTime(){return tier2EntryTime;}
    public int getTier(){return tier;}
    public int getTurnaroundTime(){
        return completionTime - arrivalTime;
    }
    public int getWaitingTime(){
        return getTurnaroundTime() - burstTime;
    }
    public int getResponseTime(){
    return startTime - arrivalTime;
    }


    // setters 
    public void setTier(int tier){
        this.tier = tier;
    }
    public void setTier2EntryTime(int time){
        this.tier2EntryTime = time;
    }
    public void updateEffectivePriority(int currentTime , int agingInterval){
        int waitingTime = currentTime - tier2EntryTime;
        int aging = waitingTime / agingInterval;
        this.effectivePriority = priority - aging;
    }
    public void setRemainingTime(int remainingTime){
        this.remainingTime = remainingTime;
    }
    public void setStartTime(int startTime){
        this.startTime = startTime;
    }
    public void setCompletionTime(int completionTime){
        this.completionTime = completionTime;
    }

    // exceute
    public void execute(){
        if(remainingTime > 0) remainingTime--;
    }

    // is finish
    public boolean isFinished() {return remainingTime == 0;}

    //change to tier 2
    public void enterTier2(int time) {
        this.tier = 2;
        this.tier2EntryTime = time;
        this.effectivePriority = priority;
    }

}
