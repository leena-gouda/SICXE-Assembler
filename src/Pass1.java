import java.io.File;
import java.io.PrintWriter;
import java.util.*;
public class Pass1 {
    static int locCount;
    static int startingAddress;
    static Instruction instruction = new Instruction();
    public static void locCounter (File intFile){
        File out_pass1 = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass1_out.txt");
        Scanner intFileReader = null;
        PrintWriter pass1Write = null;
        try {
            intFileReader  = new Scanner(intFile);
            pass1Write = new PrintWriter(out_pass1);

            String firstLine = intFileReader.nextLine();
            String[] parts = firstLine.split("\\s+");
            if (parts.length >= 2 && parts[1].equals("START")) {
                startingAddress = Integer.parseInt(parts[2], 16);
                locCount = startingAddress;
                pass1Write.println(firstLine);
                pass1Write.write(String.format("%04X\t", locCount));
                //pass1Write.println();
            }

            while(intFileReader.hasNextLine()){


                String line = intFileReader.nextLine();
                pass1Write.write(line);

                parts = line.split("\\s+");
                String label = null;
                // place instruction in inst obj based on length (checkin if theres a label)
                if(parts.length == 2){
                    instruction.Mnemonic =  parts[0];
                    instruction.operand = parts[1];
                }
                else if (parts.length == 3){
                    instruction.label =  parts[0];
                    instruction.Mnemonic =  parts[1];
                    instruction.operand = parts[2];
                }
                else {
                    instruction.Mnemonic =  parts[0];
                }

                if (instruction.Mnemonic.equals("BASE")){
                    continue;
                }

                // search for inst in list
                instruction.format = Instruction.findFormat(instruction.Mnemonic);
                instruction.opcode = Instruction.findOpcode(instruction.Mnemonic);



                // inc locount based on format
                System.out.println(instruction.format);
                if (instruction.Mnemonic.equals("END")){
                    continue;
                }
                if (instruction.format == 1){
                    locCount += 1;
                    System.out.println("in1");
                }
                else if (instruction.format == 2){
                    locCount += 2;
                    System.out.println("in2");
                }
                else if (instruction.format == 3 && !instruction.operand.startsWith("+")){
                    locCount += 3;
                }
                else {
                    locCount += 4;
                }
            }
            //write loccount + inst in file
            pass1Write.println();
            pass1Write.write(String.format("%04X\t", locCount));
        }
        catch (Exception e){
            System.out.println("Error: " + e);
            e.getMessage();
        }
        finally {
            if (intFileReader != null) {
                intFileReader.close();
            }
            if (pass1Write != null) {
                pass1Write.close();
            }
        }
    }


}
