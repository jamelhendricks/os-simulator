import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class OS {

    private static int pid_index = 0; 
    public static ProcessTable p_table = null;
    public static File template_1, template_2, template_3, template_4, template_5;
    public static File[] templates = new File[5];


    // create a PCB for some process {p}, 
    public static void init_process(Process p){
        ProcessControlBlock pcb = new ProcessControlBlock(p, pid_index, Enums.ProcessPriority.MEDIUM); // default as MEDIUM, change later
        pcb.set_memory_requirement(p.get_memory_requirement());

        pid_index = pid_index + 1;
        p_table.add_process(pcb);
    }

    // update a process state, find the process by PID
    public static boolean update_process_state(int pid, Enums.ProcessState state){
        
        if (p_table.get_pcb_by_pid(pid) != null){
            ProcessControlBlock pcb = p_table.get_pcb_by_pid(pid);
            pcb.set_state(state);
            return true;
        } else {
            return false;
        }
    }

    // create new process table for this OS instance
    public static void init_os(){
        p_table = new ProcessTable();

        template_1 = new File("templates/template_1.txt");
        template_2 = new File("templates/template_2.txt");
        template_3 = new File("templates/template_3.txt");
        template_4 = new File("templates/template_4.txt");
        template_5 = new File("templates/template_5.txt");

        templates[0] = template_1;
        templates[1] = template_2;
        templates[2] = template_3;
        templates[3] = template_4;
        templates[4] = template_5;
    }

    // OS main loop
    public static void run_os(){
        Scanner key_in = new Scanner(System.in);

        System.out.println("Welcome to Simulated OS (Java)");
        System.out.println("Author: Jamel Hendricks");
        System.out.println("Stage: Phase 1");
        System.out.println("Ready for new command: [start process] [print process table] [exit]");


        while(true){
            String user_command = key_in.nextLine();

            if (process_input(user_command) == 0){
                System.out.println("");
                System.out.println("Ready for new command: [start process] [print process table] [exit]");
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
            key_in.close();

            return 0;

        } else if (user_command.equals("print process table")){

            System.out.println("");
            System.out.println("");
            p_table.print_process_table();
            System.out.println("");
            System.out.println("");
                        
            key_in.close();

            return 0;

        } else if (user_command.equals("exit")){

            System.out.println("Quitting....");
           
            key_in.close();

            return -1;
        } else {
            System.out.println("Invalid command.");

            key_in.close();

            return 0;
        }
    }



    public static void main(String[] args) {
        init_os();
        run_os();

        //System.out.println(templates.length);
        // update_process_state(2, Enums.ProcessState.RUN);
        //p_table.print_process_table();





    }
}