import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;

public class Dispatcher {

    // confirm the next instruction on @param PCB is a CPU runnable instruction
    public static int check_runnable_instruction(ProcessControlBlock pcb){
        String instruction = pcb.get_current_instruction();
        Scanner scan = new Scanner(instruction);
        String indicator = scan.next();

        if(indicator.equals("R")){
            scan.close();
            return 0;
        } else if(indicator.equals("DONE")){
            return -1;
        } else {
            scan.close();
            return 1;
        }
    }

    // confirm the next instruction on @param PCB is a I/O waitable instruction
    public static int check_waitable_instruction(ProcessControlBlock pcb){
        String instruction = pcb.get_current_instruction();
        Scanner scan = new Scanner(instruction);
        String indicator = scan.next();

        if(indicator.equals("W")){
            scan.close();
            return 0;
        }else if(indicator.equals("DONE")){
            scan.close();
            return -1;
        } else {
            scan.close();
            return 1;
        }
    }

    // move the PCB on the ready queue to the waiting queue
    public static void wait_running_head(Queue<ProcessControlBlock> running_q, Queue<ProcessControlBlock> waiting_q){
        ProcessControlBlock head = running_q.remove();
        head.set_state(Enums.ProcessState.WAIT);
        waiting_q.add(head);
    }    
    
    // move the PCB on the waiting queue to the ready queue
    public static void ready_waiting_head(Queue<ProcessControlBlock> running_q, Queue<ProcessControlBlock> waiting_q){
        ProcessControlBlock head = waiting_q.remove();
        head.set_state(Enums.ProcessState.RUN);
        running_q.add(head);
    }

    // check how many cyles the current instruction requires on @param PCB
    public static int check_instruction_cycles(ProcessControlBlock pcb){
        String instruction = pcb.get_current_instruction();
        // System.out.println("instruction: " + instruction);
        Scanner scan = new Scanner(instruction);
        
        String bypass = scan.next(); // bypass first token (the instruction type indicator)
        if (bypass.equals("DONE")){
            return 0;
        }
        int num_cycles = Integer.parseInt(scan.next());

        scan.close();

        return num_cycles;
    }

    // print the PIDs of the PCBs on @param queues
    public static void print_qs(Queue<ProcessControlBlock> running_q, Queue<ProcessControlBlock> waiting_q){
        System.out.print("RUNNING Q: ");
        for(ProcessControlBlock pcb: running_q){
            System.out.println(pcb.get_pid());
        }
        System.out.print("WAITING Q: ");
        for(ProcessControlBlock pcb: waiting_q){
            System.out.println(pcb.get_pid());
        }
        System.out.println("");
    }

    // update the head process on @param PCB with @param cycles
    public static void update_head_process(Queue<ProcessControlBlock> generic_q, int cycles_ran){
        ProcessControlBlock head = generic_q.peek();
        head.update_cycles_ran(cycles_ran);

        // if finished this instruction load the next one
        if(head.get_cycles_ran() >= check_instruction_cycles(head)){
            head.load_next_instruction();
        }
    }

    // move the head of @param queue to the tail of the @param queue
    public static void shift_next_q(Queue<ProcessControlBlock> generic_q){
        ProcessControlBlock head = generic_q.remove();
        generic_q.add(head);
    }

    // apply round table execution to @param queue for @param cycles
    public static void exercise_q_round(Queue<ProcessControlBlock> generic_q, int cycles){
        if(generic_q.size() > 0){
            update_head_process(generic_q, cycles);
            shift_next_q(generic_q);
        }
    }

    // apply multi level execution to @param queue for @param cycles
    public static void exercise_q_multi(Queue<ProcessControlBlock> first_q, Queue<ProcessControlBlock> second_q, int cycles){
        if(first_q.size() > 0){
            update_head_process(first_q, cycles);

            if( check_runnable_instruction(first_q.peek()) == 0){
                if(second_q != null){
                    ProcessControlBlock head = first_q.remove();
                    second_q.add(head);
                } else {
                    // must be in the final q, let it run there, but round table it
                    shift_next_q(first_q);
                }
            }
        }
    }

    // terminate head process on @param queue
    public static void terminate_process(Queue<ProcessControlBlock> generic_q){
        ProcessControlBlock head = generic_q.remove();
        head.set_state(Enums.ProcessState.EXIT);
    }

    // run all processes in @param queues using round table algorithm
    public static boolean run_round_table(Queue<ProcessControlBlock> running_q, Queue<ProcessControlBlock> waiting_q, CPU cpu, int time_t){
        int operator;
        while ( running_q.size() > 0 || waiting_q.size() > 0 ){
            
            if(running_q.size() > 0){
                operator = check_runnable_instruction(running_q.peek());
                if(operator == 1){
                    wait_running_head(running_q, waiting_q);
                } else if(operator == -1){
                    terminate_process(running_q);
                }
            }

            if(waiting_q.size() > 0 ){
                operator = check_waitable_instruction(waiting_q.peek());
                if(operator == 1){
                    ready_waiting_head(running_q, waiting_q);
                } else if(operator == -1){
                    terminate_process(waiting_q);
                }
            }

            // System.out.println("PRE OPERATION QUEUE CHECK: ");
            // print_qs(running_q, waiting_q);


            if(cpu.callback_after_cycles(time_t)){
                System.out.println("\n\n");

                System.out.println("Processed " + time_t + " cycles!");              

               exercise_q_round(running_q, time_t);
               exercise_q_round(waiting_q, time_t);

                System.out.println("Operation Summary: ");
                if(running_q.size() > 0){ 
                    running_q.peek().print_pcb();
                }

                if(waiting_q.size() > 0){ 
                    waiting_q.peek().print_pcb();
                }

                System.out.println("\n\n");
            }

            // System.out.println("POST OPERATION QUEUE CHECK: ");
            // print_qs(running_q, waiting_q);

        }

        return true;
    }
    
    // run all processes in @param queues using multi level queue algorithm
    public static boolean run_multi_level(Queue<ProcessControlBlock> fast, Queue<ProcessControlBlock> mid, Queue<ProcessControlBlock> slow, Queue<ProcessControlBlock> waiting_q, CPU cpu, int time_t){
        int operator;
        while ( fast.size() > 0 || mid.size() > 0 || slow.size() > 0 || waiting_q.size() > 0){

            if (fast.size() > 0){
                operator = check_runnable_instruction(fast.peek());
                if(operator == 1){
                    wait_running_head(fast, waiting_q);
                } else if(operator == -1){
                    terminate_process(fast);
                }
            }

            if (mid.size() > 0){
                operator = check_runnable_instruction(mid.peek());
                if(operator == 1){
                    wait_running_head(mid, waiting_q);
                } else if(operator == -1){
                    terminate_process(mid);
                }
            }

            if (slow.size() > 0){
                operator = check_runnable_instruction(slow.peek());
                if(operator == 1){
                    wait_running_head(slow, waiting_q);
                } else if(operator == -1){
                    terminate_process(slow);
                }
            }

            if(waiting_q.size() > 0 ){
                operator = check_waitable_instruction(waiting_q.peek());
                if(operator == 1){
                    ready_waiting_head(fast, waiting_q);
                } else if(operator == -1){
                    terminate_process(waiting_q);
                }
            }

            if(cpu.callback_after_cycles(time_t)){
                System.out.println("\n\n");

                System.out.println("Processed " + time_t + " cycles!");              

               exercise_q_multi(fast, mid, time_t);
               exercise_q_multi(mid, slow, time_t);
               exercise_q_multi(slow, null, time_t);
               exercise_q_round(waiting_q, time_t);

                System.out.println("Operation Summary: ");
                if(fast.size() > 0){ 
                    fast.peek().print_pcb();
                }

                if(mid.size() > 0){ 
                    mid.peek().print_pcb();
                }

                if(slow.size() > 0){ 
                    slow.peek().print_pcb();
                }

                if(waiting_q.size() > 0){ 
                    waiting_q.peek().print_pcb();
                }

                System.out.println("\n\n");
            }
        }
        return true;
    }

    public Dispatcher() {}


}