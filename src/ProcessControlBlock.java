public class ProcessControlBlock {

    private int process_id;
    private Enums.ProcessState state;
    private Enums.ProcessPriority priority;
    private Process process;
    private int memory_requirement;

    private int program_counter; // TO DO
    private int[] cpu_registers; // TO DO

    // memory management information
        // TO DO
        

    // accounting information
        // TO DO: date objects?

    //private int startTime;
    //private int elapsedTime;
    //private int finishTime;
    //private int timeLimit;

    // I/O status information
        // TO DO

    public int get_pid(){
        return process_id;
    }

    public void set_process(Process p){
        this.process = p;
    }

    public void set_state(Enums.ProcessState state){
        this.state = state;
    }

    public void set_memory_requirement(int m){
        this.memory_requirement = m;
    }

    public void print_pcb(){
        System.out.println("--------------------");
        System.out.println("PID: " + process_id);
        System.out.println("State: " + state);
        System.out.println("Priority: " + priority);
        System.out.println("Memory Requirement: " + memory_requirement);
        System.out.println("--------------------");
    }

    public ProcessControlBlock(Process p, int pid, Enums.ProcessPriority priority){
        this.process = p;
        this.process_id = pid;
        this.state = Enums.ProcessState.NEW;
        this.priority = priority;
    }


}