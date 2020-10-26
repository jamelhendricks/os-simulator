import java.util.ArrayList;

public class ProcessControlBlock {

    private int process_id;
    private Enums.ProcessState state;
    private Enums.ProcessPriority priority;
    private Process process;
    private int memory_requirement;

    /*
        index 0: process code line index 
        index 1: cpu cycles ran on current instruction
    */
    private int[] program_counter = new int[2];
    private String current_instruction;

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

    

    public boolean load_next_instruction(){
        ArrayList<String> process_code = this.process.text_section;
        int next_index = this.program_counter[0] + 1;
        if(next_index >= process_code.size()){
            // return false if there are no more instructions to run
            return false;
        } else {
            // set the program counter values for the next instruction
            this.program_counter[0] += 1;
            this.program_counter[1] = 0;
            this.current_instruction = process_code.get(this.program_counter[0]);

            return true;
        }

    }

    public int get_current_code_line(){
        return program_counter[0];
    }

    public String get_current_instruction(){
        return this.current_instruction;
    }

    public int get_cycles_ran(){
        return program_counter[1];
    }

    public void update_cycles_ran(int cycles_ran){
        this.program_counter[1] += cycles_ran;
    }

    public Process get_process(){
        return this.process;
    }

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
        System.out.println("Current instruction: " + this.current_instruction);
        System.out.println("Cycles ran on current instruction: " + this.program_counter[1]);
        System.out.println("--------------------");
    }

    public ProcessControlBlock(Process p, int pid, Enums.ProcessPriority priority){
        this.process = p;
        this.process_id = pid;
        this.state = Enums.ProcessState.NEW;
        this.priority = priority;
        
        this.program_counter[0] = 0;
        this.program_counter[1] = 0;
        this.current_instruction = this.process.text_section.get(0);
    }


}