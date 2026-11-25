import gui.MainFrame;
import gui.MatrixPanel;
import model.Process;
import model.ScheduleResult;
import scheduler.ACSScheduler;
import scheduler.SRTFScheduler;

import java.util.*;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static String[] input;

    public static void main(String[] args) {
        System.out.print("1) Console  2) GUI : ");
        if (sc.nextLine().trim().equals("2")) {
            MainFrame.open();
            return;
        }

        System.out.print("Number of processes: ");
        int n = Integer.parseInt(sc.nextLine().trim());
        input = new String[n];
        for (int i = 0; i < n; i++) {
            System.out.print("Process " + (i + 1) + " (Name Arrival Burst Priority): ");
            input[i] = sc.nextLine().trim();
        }
        System.out.print("Q CS AgingInterval: ");
        int q = sc.nextInt(), cs = sc.nextInt(), aging = sc.nextInt();

        System.out.println("\n===== Preemptive SJF =====");
        ScheduleResult sjf = new SRTFScheduler().run(processes(), cs);
        printMatrix(MatrixPanel.sjfRows(sjf));
        printResults(sjf);

        System.out.println("\n===== ACS =====");
        ScheduleResult acs = new ACSScheduler(q, aging).run(processes(), cs);
        printMatrix(MatrixPanel.acsRows(acs, q, aging));
        printResults(acs);
    }

    // fresh Process objects for each scheduler
    static List<Process> processes() {
        List<Process> list = new ArrayList<>();
        for (String line : input) {
            String[] s = line.split("\\s+");
            list.add(new Process(s[0], Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3])));
        }
        return list;
    }

    static void printMatrix(List<String[]> rows) {
        System.out.println("| Time | Proc | Tier\n-------------------------------------------");
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            System.out.printf("| %-4s | %-4s | %s%n", r[0], r[1], r[2]);
            boolean lastLine = i == rows.size() - 1 || !rows.get(i + 1)[0].isEmpty();
            if (lastLine) System.out.println("-------------------------------------------");
        }
    }

    static void printResults(ScheduleResult r) {
        System.out.printf("%n%-8s %-8s %-6s %-7s %-11s %-8s %-9s%n", "Process", "Arrival", "Burst", "Finish", "Turnaround", "Waiting", "Response");
        StringJoiner tat = new StringJoiner(" + "), wt = new StringJoiner(" + ");
        for (Process p : r.getProcesses()) {
            System.out.printf("%-8s %-8d %-6d %-7d %-11d %-8d %-9d%n", p.getId(), p.getArrivalTime(), p.getBurstTime(),
                    p.getCompletionTime(), p.getTurnaroundTime(), p.getWaitingTime(), p.getResponseTime());
            tat.add("" + p.getTurnaroundTime());
            wt.add("" + p.getWaitingTime());
        }
        int n = r.getProcesses().size();
        System.out.printf("%nAverage Turnaround Time: (%s)/%d = %.2f time units%n", tat, n, r.getAverageTurnaroundTime());
        System.out.printf("Average Waiting Time: (%s)/%d = %.2f time units%n", wt, n, r.getAverageWaitingTime());
        System.out.printf("Average Response Time: %.2f time units%n", r.getAverageResponseTime());
    }
}
