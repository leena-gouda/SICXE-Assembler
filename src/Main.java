import java.io.IOException;
import java.io.*;
import java.util.*;
public class Main {
    public static void main(String[] args) {
        File code1 = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\code1.txt");
        File intFile = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\IntermediateFile.txt");
        //File code1 = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\code1.txt");
        //File intFile = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\IntermediateFile.txt");

        addIns();

        generateIntFile(code1, intFile);

        Pass1 pass1 = new Pass1();
        pass1.locCounter(intFile);

        Pass2 pass2 = new Pass2();
        pass2.openFiles();

        /*for(Instruction i : Pass2.code){
            System.out.println(i.loc + " " + i.label + " " +
                    i.Mnemonic + " " + i.operand + " " + i.opcode + " " + i.format);
        }*/


    }
    public static void generateIntFile(File inFile, File outFile) {

        Scanner scanInFile = null;
        PrintWriter writeOutFile = null;
        try {
            scanInFile = new Scanner(inFile);
            writeOutFile = new PrintWriter(outFile);
            while(true) {
                if (!scanInFile.hasNextLine())
                    break;
                String line = scanInFile.nextLine().trim();
                // replace each number and whitespace with empty string
                line = line.replaceAll("^\\d+\\s+", "");
                int commentIndex = line.indexOf(";");
                if(commentIndex != -1){
                    line = line.substring(0, commentIndex);
                }
                // removes trailing and leading whitespaces and checks if line is not empty -> writes in int. file
                if (!line.trim().isEmpty()) {
                    writeOutFile.println(line);
                }
            }
            //System.out.println("Intermediate file processed successfully");
        }
        catch (IOException e) {
            System.out.println("Error processing the file" + e.getMessage());
        }
        finally {
            //closes files
            if (scanInFile != null) {
                scanInFile.close();
            }
            if (writeOutFile != null) {
                writeOutFile.close();
            }
        }
    }
    public static void addIns(){
        Instruction.instructions.add(new Instruction("ADD", 3, "18"));
        Instruction.instructions.add(new Instruction("ADDF", 3, "58"));
        Instruction.instructions.add(new Instruction("ADDR", 2, "90"));
        Instruction.instructions.add(new Instruction("AND", 3, "40"));
        Instruction.instructions.add(new Instruction("CLEAR", 2, "B4"));
        Instruction.instructions.add(new Instruction("COMP", 3, "28"));
        Instruction.instructions.add(new Instruction("COMPF", 3, "88"));
        Instruction.instructions.add(new Instruction("COMPR", 2, "A0"));
        Instruction.instructions.add(new Instruction("DIV", 3, "24"));
        Instruction.instructions.add(new Instruction("DIVF", 3, "64"));
        Instruction.instructions.add(new Instruction("DIVR", 2, "9C"));
        Instruction.instructions.add(new Instruction("FIX", 1, "C4"));
        Instruction.instructions.add(new Instruction("FLOAT", 1, "C0"));
        Instruction.instructions.add(new Instruction("HIO", 1, "P4"));
        Instruction.instructions.add(new Instruction("J", 3, "3C"));
        Instruction.instructions.add(new Instruction("JEQ", 3, "30"));
        Instruction.instructions.add(new Instruction("JGT", 3, "34"));
        Instruction.instructions.add(new Instruction("JLT", 3, "38"));
        Instruction.instructions.add(new Instruction("JSUB", 3, "48"));
        Instruction.instructions.add(new Instruction("LDA", 3, "00"));
        Instruction.instructions.add(new Instruction("LDB", 3, "68"));
        Instruction.instructions.add(new Instruction("LDCH", 3, "50"));
        Instruction.instructions.add(new Instruction("LDF", 3, "70"));
        Instruction.instructions.add(new Instruction("LDL", 3, "08"));
        Instruction.instructions.add(new Instruction("LDS", 3, "6C"));
        Instruction.instructions.add(new Instruction("LDT", 3, "74"));
        Instruction.instructions.add(new Instruction("LDX", 3, "04"));
        Instruction.instructions.add(new Instruction("LPS", 3, "D0"));
        Instruction.instructions.add(new Instruction("MUL", 3, "20"));
        Instruction.instructions.add(new Instruction("MULF", 3, "60"));
        Instruction.instructions.add(new Instruction("MULR", 2, "98"));
        Instruction.instructions.add(new Instruction("NORM", 1, "C8"));
        Instruction.instructions.add(new Instruction("OR", 3, "44"));
        Instruction.instructions.add(new Instruction("RD", 3, "D8"));
        Instruction.instructions.add(new Instruction("RMO", 2, "AC"));
        Instruction.instructions.add(new Instruction("RSUB", 3, "4C"));
        Instruction.instructions.add(new Instruction("SHIFTL", 2, "A4"));
        Instruction.instructions.add(new Instruction("SHIFTR", 2, "A8"));
        Instruction.instructions.add(new Instruction("SIO", 1, "F0"));
        Instruction.instructions.add(new Instruction("SSK", 3, "EC"));
        Instruction.instructions.add(new Instruction("STA", 3, "0C"));
        Instruction.instructions.add(new Instruction("STB", 3, "78"));
        Instruction.instructions.add(new Instruction("STCH", 3, "54"));
        Instruction.instructions.add(new Instruction("STF", 3, "80"));
        Instruction.instructions.add(new Instruction("STI", 3, "D4"));
        Instruction.instructions.add(new Instruction("STL", 3, "14"));
        Instruction.instructions.add(new Instruction("STS", 3, "7C"));
        Instruction.instructions.add(new Instruction("STSW", 3, "E8"));
        Instruction.instructions.add(new Instruction("STT", 3, "84"));
        Instruction.instructions.add(new Instruction("STX", 3, "10"));
        Instruction.instructions.add(new Instruction("SUB", 3, "1C"));
        Instruction.instructions.add(new Instruction("SUBF", 3, "5C"));
        Instruction.instructions.add(new Instruction("SUBR", 2, "94"));
        Instruction.instructions.add(new Instruction("SVC", 2, "B0"));
        Instruction.instructions.add(new Instruction("TD", 3, "E0"));
        Instruction.instructions.add(new Instruction("TIO", 1, "F8"));
        Instruction.instructions.add(new Instruction("TIX", 3, "2C"));
        Instruction.instructions.add(new Instruction("TIXR", 2, "B8"));
        Instruction.instructions.add(new Instruction("WD", 3, "DC"));
    }
}