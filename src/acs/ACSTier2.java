package acs;

import java.util.*;

import model.Process;

public class ACSTier2 {
    private Queue<Process> tier2Queue;

    private AgingManager agingManager;

    public ACSTier2(int agingInterval){
        this.tier2Queue = new LinkedList<>(); // initilazing Queue of processes
        this.agingManager = new AgingManager(agingInterval); // initilazing aginging manager
    }
    public void addProcess(Process process){
        if(process == null)
            return;
        tier2Queue.add(process);
    }
    public void addProcesses(List<Process> processes){
        if(processes == null)
            return;
        for(Process process : processes)
            tier2Queue.add(process);
    }
}
