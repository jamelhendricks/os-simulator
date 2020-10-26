import java.lang.Thread;
import java.util.Scanner;

public class CPU {

    public static class RunClock implements Runnable {
        public Thread run_clock = new Thread();

        public final long START_TIME = System.currentTimeMillis();

        public void run(){
            System.out.println("Starting CPU clock!");
        }

        public long currentTime(){
            return (System.currentTimeMillis() ); // division factor here;
        }

        public long elapsed(long startTime){
            long instance_time = System.currentTimeMillis(); // division factor here;
            long elapsed = instance_time - startTime;
            return elapsed; // accelerated real time
        }
    }

    public static RunClock systemClock = null;

    public CPU(){
        this.systemClock = new RunClock();
        this.systemClock.run();
    }

    public boolean callback_after_cycles(long cycles_to_run){
        final long startTime = systemClock.currentTime();
        while (systemClock.elapsed(startTime) != cycles_to_run){
            // do nothing, essentially pausing CPU while this runs (simulating running the process for this many cycles)
        }

        return true; // return after @param cycles are done running
    }

}