import java.io.File;
import java.io.PrintWriter;
import java.util.*;
public class Pass1 {
     static String baseAddress = "";
     String baseOperand = "";
     int locCount;
     int startingAddress;
     Instruction instruction = new Instruction();
     public static Map<String,String> symbolTable = new HashMap<>();
     public  void locCounter (File intFile){
        //File out_pass1 = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\pass1_out.txt");
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
            }

            while(intFileReader.hasNextLine()){
                //read current line
                String line = intFileReader.nextLine();

                //if base inst write as is and continue
                if (line.contains("BASE")){
                    baseOperand = instruction.operand;
                    pass1Write.println("\t\t" + line);
                    continue;
                }

                //write locCount (of previous inst) and rewrite current instruction
                pass1Write.write(String.format("%04X\t", locCount));
                pass1Write.write(line);
                pass1Write.println();
                parts = line.split("\\s+");


                // place instruction in inst obj based on length (checking if there's a label)
                if (parts.length == 2){
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
                if (instruction.Mnemonic.equals("BASE")) {
                    continue;
                }
                if (instruction.Mnemonic.equals("END")){
                    continue;
                }

                // search for inst in list
                instruction.format = Instruction.findFormat(instruction.Mnemonic);
                instruction.opcode = Instruction.findOpcode(instruction.Mnemonic);

                // inc locCount based on format
                if (instruction.format == 1)
                    locCount++;
                else if (instruction.format == 2)
                    locCount += 2;
                else if (instruction.format == 3)
                    locCount += 3;
                else if (instruction.format == 4)
                    locCount += 4;

                else if (line.contains("RESW")){
                    // Parse the operand as decimal (since assembler directives use decimal numbers)
                    int decimalValue = Integer.parseInt(instruction.operand);
                    // Convert to bytes (RESW = 3 bytes per word)
                    int bytesToAdvance = decimalValue * 3;
                    // Advance the location counter (which is stored as hexadecimal internally)
                    locCount += bytesToAdvance;
                }
                else if (line.contains("RESB")){
                    locCount += Integer.parseInt(instruction.operand,16);
                }
                else if (line.contains("WORD")){
                    if (line.contains(",")){
                        String[] words = line.split(",");
                        locCount += words.length * 3;
                    }
                    else {
                        locCount += 3;
                    }
                }
                else if (line.contains("BYTE")){
                    switch (instruction.operand.charAt(0)){
                        // if hex value, remove X'' and divide by 2. ex: X'F12356' --> 9 - 3 = 6 --> 6 / 2 = 3 SP locCount += 3 (3 bytes)
                        case 'X' : locCount += (instruction.operand.length() - 3) / 2;
                        break;
                        // if char, remove C''. ex: C'EOF' - 3 = 3 (3 bytes; 1 byte for each char)
                        case 'C' : locCount += instruction.operand.length() - 3;
                        break;
                    }
                }

                //assign loc to instr
                instruction.loc = String.valueOf(locCount);
            }
        }
        catch (Exception e){
            System.out.println("Error: " + e);
        }
        finally {
            if (intFileReader != null) {
                intFileReader.close();
            }
            if (pass1Write != null) {
                pass1Write.close();
            }
            symTable(out_pass1);
        }
    }

    public  void symTable(File out_pass1){
        //File symFile = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\symTable.txt");
        File symFile = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\symTable.txt");

        Scanner pass1Reader = null;
        PrintWriter symFileWrite = null;

        try {
            pass1Reader = new Scanner(out_pass1);
            symFileWrite = new PrintWriter(symFile);

            while (pass1Reader.hasNextLine()){
                String line = pass1Reader.nextLine();
                String[] parts = line.split("\\s+");
                // counter - label - inst - operand
                if (parts.length == 4){
                    // counter & label
                    symFileWrite.println(parts[0] + "\t" + parts[1]);

                    symbolTable.put(parts[1], parts[0]);
                }
                // .contains in case of #
                if (baseOperand.contains(parts[1])){
                    baseAddress = parts[0];
                }
            }
        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }
        finally {
            if (pass1Reader != null) {
                pass1Reader.close();
            }
            if (symFileWrite != null) {
                symFileWrite.close();
            }
        }
    }


}
