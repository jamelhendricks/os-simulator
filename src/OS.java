import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.lang.Thread;

public class OS {

    private static int pid_index = 0; 
    public static ProcessTable p_table = null;
    public static File template_1, template_2, template_3, template_4, template_5, child_template;
    public static File[] templates = new File[6];

    public static Queue<ProcessControlBlock> waiting_q = new LinkedList<>();
    public static Queue<ProcessControlBlock> roundtable_q = new LinkedList<>();

    public static Queue<ProcessControlBlock> fast_q = new LinkedList<>();
    public static Queue<ProcessControlBlock> mid_q = new LinkedList<>();
    public static Queue<ProcessControlBlock> slow_q = new LinkedList<>();

    public static CPU cpu = null;
    public static Dispatcher dispatcher = null;

    public static void schedule_prep(){
        ArrayList<ProcessControlBlock> pcb_list = p_table.get_list(); 
        for (ProcessControlBlock pcb : pcb_list){
            waiting_q.add(pcb);
        }
    }

    // create a child process
    public static ProcessControlBlock init_child_process(){
        Process p = new Process("[Child Process]", templates[5]);
        ProcessControlBlock pcb = new ProcessControlBlock(p, pid_index, Enums.ProcessPriority.LOW); // default as LOW, change later
        p.generate_code();

        pid_index = pid_index + 1;
        p_table.add_process(pcb);

        return pcb;
    }

    // create a PCB for some process {p}, 
    public static void init_process(Process p){
        ProcessControlBlock pcb = new ProcessControlBlock(p, pid_index, Enums.ProcessPriority.MEDIUM); // default as MEDIUM, change later
        pcb.set_memory_requirement(p.get_memory_requirement());
        int skip_to = p.generate_code();
        ProcessControlBlock child = null;
        if (skip_to != -1){
            // create a child process
            child = init_child_process();
            p.generate_code_skip_to_line(skip_to);
            pcb.set_child(child);
        } else {
            // do nothing, no child process
        }

        pid_index = pid_index + 1;
        p_table.add_process(pcb);
    }

    public static void print_finish(){
        System.out.println("======================================\n\n");
        System.out.println("COMPLETED RUNS - ALL PROCESSES TERMINATED!");
        System.out.println("\n\n======================================");
    }

    // create new process table for this OS instance
    public static void init_os(){
        p_table = new ProcessTable();

        template_1 = new File("templates/template_1.txt");
        template_2 = new File("templates/template_2.txt");
        template_3 = new File("templates/template_3.txt");
        template_4 = new File("templates/template_4.txt");
        template_5 = new File("templates/template_5.txt");
        child_template = new File("templates/child_template.txt");

        templates[0] = template_1;
        templates[1] = template_2;
        templates[2] = template_3;
        templates[3] = template_4;
        templates[4] = template_5;
        templates[5] = child_template;
    }

    // OS main loop
    public static void run_os(){
        Scanner key_in = new Scanner(System.in);

        cpu = new CPU(); // instantiate CPU, starting CPU clock thread
        dispatcher = new Dispatcher(); // instantiate dispatcher


        System.out.println("==============================================================");
        System.out.println("| Welcome to Simulated OS (Java)                             |");
        System.out.println("| Author: Jamel Hendricks                                    |");
        System.out.println("| Stage: [Phase 3]                                           |");
        System.out.println("| UPDATES:                                                   |");
        System.out.println("|      + processes are able to enter uninterruptable         |");
        System.out.println("|        critical sections (round table / multi level queue) |");
        System.out.println("|      + critical instructions are constantly run on CPU,    |");
        System.out.println("|        process queues / PCB states locked by semaphores    |");
        System.out.println("==============================================================");
        System.out.println("Ready for new command: [start process] [print process table] [exit]");

        while(true){
            String user_command = key_in.nextLine();

            if (process_input(user_command) == 0){
                System.out.println("");
                if (p_table.get_count() > 0){
                    System.out.println("Ready for new command: [start process] [print process table] [run] [exit]");
                } else {
                    System.out.println("Ready for new command: [start process] [print process table] [exit]");
                }
            } else if ( process_input(user_command) == -1){
                System.exit(0);
            } else {
                System.out.println("");
                System.out.println("Unknown error has occurred. Exiting OS.");
                System.exit(0);
            }
        }

    }

    // process command line OS input
    public static int process_input(String user_command){
        Scanner key_in = new Scanner(System.in);
        Scanner line_reader;

        if (user_command.equals("start process")){
            System.out.println("Enter process name & template # (Format:[NAME] [#]): ");
            String user_in = key_in.nextLine();

            line_reader = new Scanner(user_in);

            String p_name = line_reader.next();
            int t_index = line_reader.nextInt();

            Process p = new Process(p_name, templates[t_index-1]); // off by one for template num, subtract one
            init_process(p);

            line_reader.close();

            return 0;

        } else if (user_command.equals("print process table")){

            System.out.println("");
            System.out.println("");
            p_table.print_process_table();
            System.out.println("");
            System.out.println("");
                        
            return 0;

        } else if (user_command.equals("run")){
            schedule_prep();

            System.out.println("Select a scheduling algorithm: [round table] [multi level]");
            String schedule_algo = key_in.nextLine();

            if (schedule_algo.equals("round table")){
                System.out.println("\n\nRunning processes with round table algorithm");
                dispatcher.run_round_table(roundtable_q, waiting_q, cpu, 50);
                p_table.print_process_table();
                print_finish();
            } else if (schedule_algo.equals("multi level")) {
                System.out.println("\n\nRunning processes with round table algorithm");
                dispatcher.run_multi_level(fast_q, mid_q, slow_q, waiting_q, cpu, 50);
                p_table.print_process_table();
                print_finish();
            }

            return 0;
        }else if (user_command.equals("exit")){

            System.out.println("Quitting....");
           
            key_in.close();

            return -1;
        } else {
            System.out.println("Invalid command.");

            return 0;
        }
    }

    public static void main(String[] args) {
        init_os();
        run_os();

    }
}