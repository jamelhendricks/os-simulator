/*

2.1 Process class
Class for creating processes as new instances of it with all the necessary
properties of processes and capabilities for extending them with future
functionalities (e.g., memory, I/O, etc.)

*/
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;
import java.io.FileNotFoundException;

public class Process {

    private String process_name;
    public ArrayList<String> text_section;
    private String[] data_section;
    private File template;
    private int memory_requirement;
    

    public String getProcessName(){
        return this.process_name;
    }

    public ArrayList<String> get_text_section(){
        return text_section;
    }

    public int get_memory_requirement(){
        return memory_requirement;
    }

    public int generate_code(){
        Scanner file_in;
        try {
            file_in = new Scanner(this.template);
        } catch(FileNotFoundException e){
            System.out.println("File not found. Could not generate program from template.");
            System.exit(0);
            return -1;
        }

        String next_instruction = "";
        int next_cycles = 0;
        memory_requirement = file_in.nextInt();
        int num_skips = 0;

        while (file_in.hasNextLine()){
            next_instruction = file_in.nextLine();
            num_skips = num_skips + 1;

            if (next_instruction.equals("OUT")){
                text_section.add("PRINTPCB");
            } else if (next_instruction.contains("CALCULATE")){
                Scanner read_line = new Scanner(next_instruction);
                read_line.next(); // skip the next token (calculate)

                int temp_cycles = read_line.nextInt();
                text_section.add("R " + generate_calc(temp_cycles)); // R to signal ready queue

                read_line.close();
            } else if (next_instruction.equals("I/O")){
                text_section.add("W " + generate_io()); // W to signal wait queue
            } else if (next_instruction.equals("<CRITICAL>")){
                next_instruction = file_in.nextLine(); // skip to the actual instruction, not <CRITICAL>
                Scanner read_line = new Scanner(next_instruction);
                read_line.next(); // skip the nex token (calculate)
                int temp_cycles = read_line.nextInt();

                text_section.add("C " + generate_critical(temp_cycles)); // C to signal critical section
                read_line.close();

            } else if (next_instruction.equals("</CRITICAL>")) {
                // do nothing
            } else if (next_instruction.equals("FORK")){
                // do nothing, child process is created in init_process() > OS
                return num_skips;
            }
        }

        text_section.add("DONE");
        file_in.close();

        return -1;
    }

    public void generate_code_skip_to_line(int skip_to_this_line){
        Scanner file_in;
        try {
            file_in = new Scanner(this.template);
        } catch(FileNotFoundException e){
            System.out.println("File not found. Could not generate program from template.");
            return;
        }

        String next_instruction = "";
        int next_cycles = 0;
        memory_requirement = file_in.nextInt();
        
        for (int i = 0; i < skip_to_this_line -1; i++){ // subtract one bc we will skip one line in the loop
            next_instruction = file_in.nextLine();
        }

        while (file_in.hasNextLine()){
            next_instruction = file_in.nextLine();

            if (next_instruction.equals("OUT")){
                text_section.add("PRINTPCB");
            } else if (next_instruction.contains("CALCULATE")){
                Scanner read_line = new Scanner(next_instruction);
                read_line.next(); // skip the next token (calculate)

                int temp_cycles = read_line.nextInt();
                text_section.add("R " + generate_calc(temp_cycles)); // R to signal ready queue

                read_line.close();
            } else if (next_instruction.equals("I/O")){
                text_section.add("W " + generate_io()); // W to signal wait queue
            } else if (next_instruction.equals("<CRITICAL>")){
                next_instruction = file_in.nextLine(); // skip to the actual instruction, not <CRITICAL>
                Scanner read_line = new Scanner(next_instruction);
                read_line.next(); // skip the nex token (calculate)
                int temp_cycles = read_line.nextInt();

                text_section.add("C " + generate_critical(temp_cycles)); // C to signal critical section
                read_line.close();

            } else if (next_instruction.equals("</CRITICAL>")) {
                // do nothing
            } else if (next_instruction.equals("FORK")){
                // do nothing, child process is created in init_process() > OS
            }
        }

        text_section.add("DONE");
        file_in.close();
    }

    public int generate_io(){
        Random random = new Random();
        int generated_num = random.nextInt(200);
        if (generated_num == 0){
            generated_num = 95;
        }
        return generated_num;
    }

    public int generate_calc(int temp_cycles){
        Random random = new Random();
        int multiplier = random.nextInt(5);
        if (multiplier == 0){
            multiplier = 5;
        } 
        return temp_cycles * multiplier;
    }

    public int generate_critical(int temp_cycles){
        Random random = new Random();
        int multiplier = random.nextInt(15);
        if (multiplier == 0){
            multiplier = 15;
        }

        return (temp_cycles * multiplier) + 800;
    }

    public void print_text_section(){
        System.out.println(Arrays.toString(text_section.toArray()));
    }

    Process(String name,File template){
        this.process_name = name;
        this.template = template;
        this.text_section = new ArrayList<String>();

        generate_code(); // create text section from template
    }


}
