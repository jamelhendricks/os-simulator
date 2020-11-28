import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.lang.Thread;

public class Dispatcher {

    private static Semaphore access = new Semaphore(1);
    private static CPUCache cpuCache = null;
    private static MainMemory ram = null;

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
        } else if(indicator.equals("C")){
            scan.close();
            return 9;
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
        Process p = head.get_process();

        head.set_state(Enums.ProcessState.WAIT);
        waiting_q.add(head);
    }    
    
    // move the PCB on the waiting queue to the ready queue
    public static void ready_waiting_head(Queue<ProcessControlBlock> running_q, Queue<ProcessControlBlock> waiting_q, MainMemory ram){
        ProcessControlBlock head = waiting_q.remove();
        Process p = head.get_process();

        head.set_state(Enums.ProcessState.RUN);
        running_q.add(head);

        if (!ram.checkInMemory(p)){
            ram.addProcess(p);
        }
    }

    // check how many cyles the current instruction requires on @param PCB
    public static int check_instruction_cycles(ProcessControlBlock pcb){
        String instruction = pcb.get_current_instruction();
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
    // returns true if completed this instruction and moved to next
    public static boolean update_head_process(Queue<ProcessControlBlock> generic_q, int cycles_ran){
        ProcessControlBlock head = generic_q.peek();
        head.update_cycles_ran(cycles_ran);

        // if finished this instruction load the next one
        if(head.get_cycles_ran() >= check_instruction_cycles(head)){
            head.load_next_instruction();
            return true;
        } else {
            return false;
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

    // apply round table execution for CRITICAL section to @param queue for @param cycles
    // returns true if completed instruction
    public static boolean exercise_q_round_CRITICAL(Queue<ProcessControlBlock> generic_q, int cycles){
        if(generic_q.size() > 0 && generic_q.peek().get_cycles_ran() < check_instruction_cycles(generic_q.peek())){
            return update_head_process(generic_q, cycles);
        } else {
            return true;
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

    // apply multilevel execution for CRITICAL section to @param queue for @param cycles
    // returns true if completed instruction
    public static boolean exercise_q_multi_CRITICAL(Queue<ProcessControlBlock> any_q, int cycles){
        if(any_q.size() > 0 && any_q.peek().get_cycles_ran() < check_instruction_cycles(any_q.peek())){
            return update_head_process(any_q, cycles);
        } else {
            return true;
        }
    }

    // terminate head process on @param queue
    public static int terminate_process(Queue<ProcessControlBlock> generic_q){
        // if (generic_q.size() == 0){
        //     return -1;
        // }

        ProcessControlBlock head = generic_q.remove();
        head.set_state(Enums.ProcessState.EXIT);

        if (head.child_process_exists()){
            // find the child pcb and remove it,
            // return the queue
            return head.get_child_PID();

        }
        return -1;
    }

    // searches this queue for a pcb with a matching pid and removes it
    public static Queue<ProcessControlBlock> search_and_terminate(Queue<ProcessControlBlock> generic_q, int pid){
            // if (generic_q.size() == 0){
            //     return generic_q;
            // }

            for (int i = 0; i < generic_q.size(); i++){
                ProcessControlBlock test = generic_q.remove(); // get the next item in queue
                if (test.get_pid() == pid){
                    test.set_state(Enums.ProcessState.EXIT);
                    return generic_q; // stop removing here and return the modified queue
                } else {
                    generic_q.add(test); // add the test pcb to the back and keep iterating
                }
            }

        return generic_q;
    }



    // run all processes in @param queues using round table algorithm
    public static boolean run_round_table(Queue<ProcessControlBlock> running_q, Queue<ProcessControlBlock> waiting_q, CPU cpu, int time_t){
        int operator;
        while ( running_q.size() > 0 || waiting_q.size() > 0 ){
            System.out.println("Running q: " + running_q.size());
            System.out.println("Waiting q: " + waiting_q.size());
            System.out.println("Permits: " + access.availablePermits());

            if(running_q.size() > 0){
                operator = check_runnable_instruction(running_q.peek());
                if(operator == 1){
                    wait_running_head(running_q, waiting_q);
                } else if(operator == -1){

                    // @child_pid = PID of child process || -1 if no child
                    int child_pid = terminate_process(running_q);
                    if (child_pid != -1){

                        // search the other queues for the child and terminate it
                        running_q = search_and_terminate(running_q, child_pid);
                        waiting_q = search_and_terminate(waiting_q, child_pid);

                    }
                } else if(operator == 9){
                    // we have encountered / are in a critical section instruction
                    if (access.availablePermits() == 1){
                        try{
                            access.acquire();
                            System.out.println("\n\njust called acquire!");
                        } catch (InterruptedException e){
                            System.out.println("Tried to acquire a permit when there was none!");
                        }
                    }
                }
            }

            if(waiting_q.size() > 0 ){
                operator = check_waitable_instruction(waiting_q.peek());
                if(operator == 1){
                    ready_waiting_head(running_q, waiting_q, ram);
                    System.out.println("Just called ready");
                } else if(operator == -1){
                    // @child_pid = PID of child process || -1 if no child
                    int child_pid = terminate_process(waiting_q);
                    if (child_pid != -1){
                        // search the other queues for the child and terminate it
                        waiting_q = search_and_terminate(waiting_q, child_pid);
                        running_q = search_and_terminate(running_q, child_pid);
                    }
                }
            }

            if(cpu.callback_after_cycles(time_t)){
                System.out.println("\n\n");

                System.out.println("Processed " + time_t + " cycles!");              

                // if permit is available, cases:
                //     a) critical section is over
                //     b) we were not in a critical section
                // permit not available, things that need to happen now:
                //     a) update the PCB -> done in the CRITICAL exercise method
                //     b) shift to next PCB
                if (access.availablePermits() == 1){

                    if (running_q.size() > 0){
                        cpuCache.addProcess(running_q.peek().get_process());
                    }

                    exercise_q_round(running_q, time_t);
                    exercise_q_round(waiting_q, time_t);
                } else {
                    System.out.println("Waiting for critical section to complete!");
                    
                    if (running_q.size() > 0){
                        cpuCache.addProcess(running_q.peek().get_process());
                    }
                    boolean complete_critical = exercise_q_round_CRITICAL(running_q, time_t);

                    System.out.println("Critical section exercised!");

                    // release the semaphore lock if the critical instruction was completed
                    if(complete_critical){
                        access.release();
                        System.out.println("Semaphore condition met! Access lock released!");
                    }

                }


                System.out.println("Operation Summary: ");
                if(running_q.size() > 0){ 
                    running_q.peek().print_pcb();
                }

                if(waiting_q.size() > 0){ 
                    waiting_q.peek().print_pcb();
                }

                if((running_q.size() == 0) || (waiting_q.size() == 0)) {
                    System.out.println("One of the queues is empty!");
                }

                System.out.println("Memory: " + (ram.total_memory - ram.remainingMemory) + " / " + ram.total_memory);
                System.out.println("CPU Cache: " + CPUCache.total_cache + " / " + CPUCache.MAX_CACHE_NUM);

                System.out.println("\n\n");
            }

        }

        System.out.println("Exited running processes!");
        
        return true;
    }
    
    // run all processes in @param queues using multi level queue algorithm
    public static boolean run_multi_level(Queue<ProcessControlBlock> fast, Queue<ProcessControlBlock> mid, Queue<ProcessControlBlock> slow, Queue<ProcessControlBlock> waiting_q, CPU cpu, int time_t){
        int operator;
        Queue<ProcessControlBlock> critical_q = null; // holder for the queue that contains the crit section process

        while ( fast.size() > 0 || mid.size() > 0 || slow.size() > 0 || waiting_q.size() > 0){

            System.out.println("Fast q: " + fast.size());
            System.out.println("Mid q: " + mid.size());
            System.out.println("Slow q: " + slow.size());
            System.out.println("Waiting q: " + waiting_q.size());
            System.out.println("Permits: " + access.availablePermits());

            if (fast.size() > 0){
                operator = check_runnable_instruction(fast.peek());
                if(operator == 1){
                    wait_running_head(fast, waiting_q);
                } else if(operator == -1){
                    
                    int child_pid = terminate_process(fast);
                    if (child_pid != -1){
                        // search the other queues for the child and terminate it
                        fast = search_and_terminate(fast, child_pid);
                        mid = search_and_terminate(mid, child_pid);
                        slow = search_and_terminate(slow, child_pid);
                        waiting_q = search_and_terminate(waiting_q, child_pid);
                    }
                } else if(operator == 9){
                    // we have encountered / are in a critical section instruction
                    if (access.availablePermits() == 1){
                        try{
                            access.acquire();
                            System.out.println("\n\njust called acquire!");
                            critical_q = fast;
                        } catch (InterruptedException e){
                            System.out.println("Tried to acquire a permite when there was none!");
                        }
                    }
                }
            }

            if (mid.size() > 0){
                operator = check_runnable_instruction(mid.peek());
                if(operator == 1){
                    wait_running_head(mid, waiting_q);
                } else if(operator == -1){
                    int child_pid = terminate_process(mid);
                    if (child_pid != -1){
                        // search the other queues for the child and terminate it
                        fast = search_and_terminate(fast, child_pid);
                        mid = search_and_terminate(mid, child_pid);
                        slow = search_and_terminate(slow, child_pid);
                        waiting_q = search_and_terminate(waiting_q, child_pid);
                    }
                } else if(operator == 9){
                    // we have encountered / are in a critical section instruction
                    if (access.availablePermits() == 1){
                        try{
                            access.acquire();
                            System.out.println("\n\njust called acquire!");
                            critical_q = mid;
                        } catch (InterruptedException e){
                            System.out.println("Tried to acquire a permite when there was none!");
                        }
                    }
                }
            }

            if (slow.size() > 0){
                operator = check_runnable_instruction(slow.peek());
                if(operator == 1){
                    wait_running_head(slow, waiting_q);
                } else if(operator == -1){
                    int child_pid = terminate_process(slow);
                    if (child_pid != -1){
                        // search the other queues for the child and terminate it
                        fast = search_and_terminate(fast, child_pid);
                        mid = search_and_terminate(mid, child_pid);
                        slow = search_and_terminate(slow, child_pid);
                        waiting_q = search_and_terminate(waiting_q, child_pid);
                    }
                } else if(operator == 9){
                    // we have encountered / are in a critical section instruction
                    if (access.availablePermits() == 1){
                        try{
                            access.acquire();
                            critical_q = slow;
                            System.out.println("\n\njust called acquire!");
                        } catch (InterruptedException e){
                            System.out.println("Tried to acquire a permite when there was none!");
                        }
                    }
                }
            }

            if(waiting_q.size() > 0 ){
                operator = check_waitable_instruction(waiting_q.peek());
                if(operator == 1){
                    ready_waiting_head(fast, waiting_q, ram);
                } else if(operator == -1){
                    int child_pid = terminate_process(waiting_q);
                    if (child_pid != -1){
                        // search the other queues for the child and terminate it
                        fast = search_and_terminate(fast, child_pid);
                        mid = search_and_terminate(mid, child_pid);
                        slow = search_and_terminate(slow, child_pid);
                        waiting_q = search_and_terminate(waiting_q, child_pid);
                    }
                }
            }

            if(cpu.callback_after_cycles(time_t)){
                System.out.println("\n\n");

                System.out.println("Processed " + time_t + " cycles!");              

               


                // if permit is available, cases:
                //     a) critical section is over
                //     b) we were not in a critical section
                // permit not available, things that need to happen now:
                //     a) update the PCB -> done in the CRITICAL exercise method
                //     b) shift to next PCB
                if (access.availablePermits() == 1){

                    if (fast.size() > 0){
                        cpuCache.addProcess(fast.peek().get_process());
                    }

                    exercise_q_multi(fast, mid, time_t);

                    if (mid.size() > 0){
                        cpuCache.addProcess(mid.peek().get_process());
                    }

                    exercise_q_multi(mid, slow, time_t);


                    if (slow.size() > 0){
                        cpuCache.addProcess(slow.peek().get_process());
                    }
                    exercise_q_multi(slow, null, time_t);

                    exercise_q_round(waiting_q, time_t);
                } else {                    
                    boolean complete_critical = exercise_q_multi_CRITICAL(critical_q, time_t);
                    // release the semaphore lock if the critical instruction was completed
                    if(complete_critical){
                        access.release();
                        critical_q = null;
                        System.out.println("Semaphore condition met! Access lock released!");
                    }

                }

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

                System.out.println("Memory: " + (ram.total_memory - ram.remainingMemory) + " / " + ram.total_memory);
                 System.out.println("CPU Cache: " + CPUCache.total_cache + " / " + CPUCache.MAX_CACHE_NUM);


                System.out.println("\n\n");
            }        
        }

        System.out.println("Exited running processes!");
        
        return true;
    }

    public Dispatcher(CPUCache c, MainMemory m) {
        cpuCache = c;
        ram = m;
    }


}