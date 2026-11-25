package acs;
import model.Process;
public class ACSTier1{
    private final int quantum;

    public ACSTier1(int quantum){
        if(quantum <= 0){
            throw new IllegalArgumentException(
                    "Quantum must be positive"
            );
        }
        this.quantum = quantum;
    }


    public int executeQuantum(Process process) {

        int executionTime = Math.min(quantum, process.getRemainingTime());

        process.setRemainingTime(
                process.getRemainingTime() - executionTime
        );

        return executionTime;
    }

}
