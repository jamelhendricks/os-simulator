import java.util.*;

// ready state processes can go in memory

public class MainMemory {

    public static final int total_memory = 4000; // megabytes, so 4gb of RAM
    public final int kernal_requirement = 1000; // 1gb required for OS to run
    public static int remainingMemory = 4000; // initially all memory available
    public static ArrayList<Process> memoryProcessCatalog = new ArrayList<Process>();

    public static boolean checkInMemory(Process p){
        if (memoryProcessCatalog.contains(p)){
            return true;
        } else {
            return false;
        }
    }

    public static void addProcess(Process p){
        int requested_memory = p.get_memory_requirement();

        // keep removing old processes sequentially until the new one can fit
        while (remainingMemory < requested_memory){
            remove_oldest_process();
            System.out.println("Freeing main memory to add a process!");
        }

        memoryProcessCatalog.add(p);
        remainingMemory = remainingMemory - requested_memory;
        System.out.println("Added a process to main memory!");
    }


    public static void removeProcess(Process p){
        memoryProcessCatalog.remove(p);

        int free_memory = p.get_memory_requirement();
        remainingMemory = remainingMemory + free_memory;
    }

    public static int getRemainingMemory(){
        return remainingMemory;
    }

    public static void remove_oldest_process(){
        Process oldest_process = memoryProcessCatalog.get(0);

        removeProcess(oldest_process); // frees the memory because we used our remove process method
    }

    public MainMemory() {
        remainingMemory = remainingMemory - kernal_requirement;
    }

    public static void main(String[] args) {
        
    }
}