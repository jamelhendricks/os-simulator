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
    private ArrayList<String> text_section;
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

    public void generate_code(){
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

        while (file_in.hasNextLine()){
            next_instruction = file_in.nextLine();

            if (next_instruction.equals("OUT")){
                text_section.add("PRINTPCB");
            } else if (next_instruction.contains("CALCULATE")){
                Scanner read_line = new Scanner(next_instruction);
                read_line.next(); // skip the next token (calculate)

                int temp_cycles = read_line.nextInt();
                text_section.add("RUN" + generate_calc(temp_cycles));

                read_line.close();
            } else if (next_instruction.equals("I/O")){
                text_section.add("RUN" + generate_io());
            }
        }

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