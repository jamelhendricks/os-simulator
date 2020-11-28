import java.lang.Math; 
import java.util.*;

public class CPUCache {

    // maximum of 100 * 4MB (400MB) in the CPU cache, the max a process can be is also 400mb
    // each cache is 1000 times smaller than the main memory, as the requirements state
    public static final int MAX_CACHE_NUM = 100; 
    public static int total_cache = 0; 

    public static LinkedHashMap<Process, ArrayList<Cache>> cpuCache = new LinkedHashMap<Process, ArrayList<Cache>>();
    

    public static int calcCacheNeed(Process p){
        int requestedMemory = p.get_memory_requirement();
        int cacheSize = 4; // each cache is 4mb
        int numCache = (int) Math.ceil( (double) requestedMemory / (double) cacheSize);

        return numCache;
    }

    public static void removeOldestProcess(){
        Map.Entry<Process,ArrayList<Cache>> entry = cpuCache.entrySet().iterator().next();

        Process key = entry.getKey();
        ArrayList<Cache> value = entry.getValue();
        int cachesToFree = value.size();
        int memoryToFree = cachesToFree * 4; // each cache is 4mb

        cpuCache.remove(entry);
        total_cache = total_cache - memoryToFree;

    }

    public static void addProcess(Process p){

        int cacheNeed = calcCacheNeed(p);

        // make sure there's enough space to add the newest program to the cache
        while (cacheNeed + total_cache > MAX_CACHE_NUM){
            removeOldestProcess();
        }

        ArrayList<Cache> processCacheList = new ArrayList<Cache>();

        for (int i = 0; i < cacheNeed; i++){
            Cache c = new Cache();
            processCacheList.add(c);
        }

        cpuCache.put(p, processCacheList);
        total_cache = total_cache + cacheNeed;
                    
    }

    public CPUCache(){}

    public static void main(String[] args) {
        
    }
}