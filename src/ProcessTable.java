import java.util.LinkedList;
import java.util.ArrayList;

public class ProcessTable {

    //private final int MAX_PROCESS_NUM = 10;
    private ArrayList<ProcessControlBlock> pcb_list = new ArrayList<ProcessControlBlock>();
    //private int process_count = 0;

    // default constructor
    public ProcessTable(){}

    // clear entire list
    public void init_table(){
        for (int i = 0; i < pcb_list.size(); i++){
            pcb_list = null;
        }
    }

    public int get_count(){
        return pcb_list.size();
    }

    public ArrayList<ProcessControlBlock> get_list(){
        return pcb_list;
    }

    // add a new pcb w/ process to 
    public void add_process(ProcessControlBlock pcb){

        // intended for array index, so off by 1, don't need to subtract 1!
        // int new_process_index = process_count;

        // pcb_list[new_process_index] = pcb; outdated array implementation
        pcb_list.add(pcb);
        // process_count = process_count + 1;
    }

    // clear the pcb from the table, [ does not ((terminate)) process ]
    public void remove_process(int pid){
        int targetIndex;
        for (targetIndex = 0; targetIndex < pcb_list.size(); targetIndex++){
            // if (pcb_list[targetIndex].get_pid() == pid){ outdated array implementation
            if (pcb_list.get(targetIndex).get_pid() == pid){
                break;
            }
        }

        // if ((targetIndex == pcb_list.length) && (pcb_list[targetIndex].get_pid() != pid)){ outdated array implementation
        if ((targetIndex == pcb_list.size()) && (pcb_list.get(targetIndex).get_pid() != pid)){
            System.out.println("No matching PID: " + pid + "!");
            return;
        }

        // pcb_list[targetIndex] = null; // clear the pcb from the process table outdated array implementation
        pcb_list.remove(targetIndex); // clear the pcb from the process table
        // process_count = process_count - 1;
    }

    public ProcessControlBlock get_pcb_by_pid(int pid){
        ProcessControlBlock return_block = null;

        // only call get_pid() on NON-NULL PCB  (i < process_count NOT pcb_list.length)
        for (int i = 0; i < pcb_list.size(); i++){
            if (pcb_list.get(i).get_pid() == pid){
                return_block = pcb_list.get(i);
            }
        }

        return return_block;
    }

    // dont need with array list implementation
    // public void restructure_list(){
    //     ProcessControlBlock[] new_pcb_list = new ProcessControlBlock[MAX_PROCESS_NUM];
    //     int list_index = 0;

    //     for (int i = 0; i < pcb_list.length; i++){
    //         if (pcb_list[i] != null){
    //             new_pcb_list[list_index] = pcb_list[i];
    //             list_index++;
    //         }
    //     }

    //     pcb_list = new_pcb_list;
    // }

    public void print_process_table(){

        System.out.println("============================\nOS PROCESS TABLE\n============================");

        for (int i = 0; i < pcb_list.size(); i++){
            // if(pcb_list[i] == null){ outdated array implementation
            if(pcb_list.get(i) == null){
                System.out.println(i + ": [NULL]");
            } else {
                System.out.println(i + ":");
                pcb_list.get(i).print_pcb();
            }
        }
    }

}