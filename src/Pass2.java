import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Pass2 {

    public static List<Instruction> code = new ArrayList<>();
    public List<String> objectCodes = new ArrayList<>();
    String objCode;
    String PC;
    public Pass2() { }


    public void openFiles(){
        File pass1_out = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\pass1_out.txt");
        File pass2_out = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\pass2_out.txt");
        //File pass1_out = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass1_out.txt");
        //File pass2_out = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass2_out.txt");

        handleCode(pass1_out, pass2_out);
    }

    private void handleCode(File pass1_out, File pass2_out){

        Scanner pass1Scan = null;
        PrintWriter pass2Write = null;

        try {
            pass1Scan =  new Scanner(pass1_out);
            pass2Write = new PrintWriter(pass2_out);
            Instruction i = null;
            while(pass1Scan.hasNextLine()){

                i = new Instruction();
                String line = pass1Scan.nextLine();
                String [] parts = line.split("\\s+");

                // assign parts of each line to each instr object

                // for start dir
                if (parts.length == 3 && parts[1].equals("START")){
                    i.label = parts[0];
                    i.Mnemonic =  parts[1];
                    i.operand = parts[2];
                }
                // if there's no label
                else if (parts.length == 3){
                    i.loc =  parts[0];
                    i.Mnemonic =  parts[1];
                    i.operand = parts[2];
                }
                // if there's a label
                else if (parts.length == 4){
                    i.loc =  parts[0];
                    i.label =  parts[1];
                    i.Mnemonic = parts[2];
                    i.operand = parts[3];
                }
                // for base dir
                else if (line.contains("BASE")){
                    i.Mnemonic =  parts[0];
                    i.operand =  parts[1];
                    i.base = i.loc;
                }
                // for f1 instructions
                else if (parts.length == 2){
                    i.loc =  parts[0];
                    i.Mnemonic =  parts[1];
                }

                // assign format and opcode to each inst obj
                i.format = Instruction.findFormat(i.Mnemonic);
                i.opcode = Instruction.findOpcode(i.Mnemonic);

                // add each instr obj to code
                Pass2.code.add(i);

            }
            for (Instruction instr : code){

                // go to generateObjCode method
                objCode = generateObjCode(instr);

                //System.out.println(instr.Mnemonic + " " + objCode);

                // write in file
                if (instr.label == null){
                    instr.label = "";
                }
                if (instr.loc == null){
                    instr.loc = "";
                }
                if (instr.operand == null){
                    instr.operand = "";
                }
                if (!objCode.isEmpty()){
                    objectCodes.add(objCode);
                }

                pass2Write.write(instr.loc + "\t" + instr.label + "\t" + instr.Mnemonic + "\t" +
                        instr.operand + "\t" + objCode + "\n");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            //System.out.println(e + "here");
        }
        finally {
            if (pass1Scan != null) {
                pass1Scan.close();
            }
            if (pass2Write != null) {
                pass2Write.close();
            }
        }
    }

    private String generateObjCode(Instruction i){
        // no obj code for these directives
        if (i.Mnemonic.equals("START") || i.Mnemonic.equals("END") || i.Mnemonic.equals("BASE")
                || i.Mnemonic.equals("RESW") || i.Mnemonic.equals("RESB")){
            return "";
        }
        else if (i.Mnemonic.equals("BYTE")){
            return handleByteDirective(i.operand);
        }
        else if (i.Mnemonic.equals("WORD")){
            // if there is more than 1 word
            if (i.operand.contains(",")) {
                // seperate at ,
                String[] words = i.operand.split(",");
                StringBuilder objCode = new StringBuilder();
                // find objCode for each individually then concatenate
                for (String word : words) {
                    objCode.append(handleWordDirective(word));
                }
                return objCode.toString();
            }
            return handleWordDirective(i.operand);
        }
        // if regular instruction
        else {
            return switch (i.format) {
                case 1 -> i.opcode; // f1 -> opcode only
                case 2 -> handleFormat2(i);
                case 3 -> handleFormat3(i);
                //case 4 -> handleFormat4(i);
                default -> "";
            };
        }
    }

    private String handleByteDirective(String operand) {
        // remove X or C prefix
        String pureOperand = operand.substring(2, operand.length() - 1);
        if (operand.startsWith("C")){
            StringBuilder objCode = new StringBuilder();
            // converts string to byte array and loops through it
            for (byte b : pureOperand.getBytes()) {
                // converts byte ASCII to hex string and uppercase
                String hex = Integer.toHexString(b & 0xFF).toUpperCase();
                if (hex.length() == 1){
                    objCode.append('0');
                }
                // concatenates to string builder
                objCode.append(hex);
            }
            return objCode.toString();
        }
        return pureOperand;
    }

    private String handleWordDirective(String operand) {
        // converts string to int
        int value = Integer.parseInt(operand);
        // formats int into hex string
        return String.format("%06X", value);
    }

    private String handleFormat2(Instruction i){
        // split operand at ,
        String[] regs = i.operand.split(",");
        String reg1 = registerCode(regs[0]);
        // if there is reg 2, include in objCode. if not, return 0
        String reg2 = regs.length > 1 ? registerCode(regs[1]) : "0";
        return i.opcode + reg1 + reg2;
    }

    private String registerCode(String reg) {
        return switch (reg) {
            case "A" -> "0";
            case "X" -> "1";
            case "L" -> "2";
            case "B" -> "3";
            case "S" -> "4";
            case "T" -> "5";
            case "F" -> "6";
            default -> "";
        };
    }

    private String handleFormat3(Instruction i){
        char e = '0';
        char p = '0';
        char b = '0';
        if (i.Mnemonic.equals("RSUB")){
            return "4F0000";
        }
        String opcode = hexToBinary(i.opcode);
        opcode = opcode.substring(0, opcode.length() - 2);

        String NIX = setNIX(i);

        if (NIX.equals("010")){
            String immOp = i.operand.substring(1);
            if (immOp.matches("\\d+")) {
                p = '0';
                b = '0';
                return binaryToHex(opcode + NIX + b + p + e) + to3DigitHex(Integer.parseInt(immOp));
            }
        }

        String addMode = setPB(i);
        if (addMode.startsWith("p")){
            p = '1';
        }
        else b = '1';
        String disp = addMode.substring(1);
        objCode = binaryToHex(opcode + NIX + b + p + e) + disp;
        return objCode;
    }

    private String hexToBinary(String hex) {
        // Remove any 'x' or 'h' suffix if present
        hex = hex.replaceAll("[xXhH]", "");

        // Convert to binary with leading zeros
        return String.format("%8s", Integer.toBinaryString(Integer.parseInt(hex, 16))).replace(' ', '0');
    }

    private String binaryToHex(String binary) {
        // Parse binary (12 bits) and format as 3 uppercase hex digits
        int decimal = Integer.parseInt(binary, 2);
        return String.format("%03X", decimal);
    }

    private String setNIX (Instruction inst){
        char n;
        char i;
        char x;
        if (inst.operand.contains(",X")){
            x = '1';
        }
        else x = '0';
        if (inst.operand.startsWith("@")){
            n = '1';
            i = '0';
        }
        else if (inst.operand.startsWith("#"))
        {
            n = '0';
            i = '1';
        }
        else {
            n = '1';
            i = '1';
        }
        return "" + n + i + x;
    }

    private String setPB (Instruction inst){
        String flagPlusDisp = "";
        int j = Pass2.code.indexOf(inst);

        if (!Pass2.code.get(j + 1).Mnemonic.equals("BASE"))
            PC = Pass2.code.get(j + 1).loc;
        else PC = Pass2.code.get(j + 2).loc;
        int PCInt = Integer.parseInt(PC, 16);


        String TA = getTA(inst.operand);
        int TAInt = Integer.parseInt(TA, 16);

        int displacement = TAInt - PCInt;
        if (displacement >= -2048 && displacement <= 2047) {
            flagPlusDisp = "p";
        }
        else {
            int base  = Integer.parseInt(Pass1.baseAddress, 16);
            displacement = TAInt - base;
            if (displacement >= 0 && displacement <= 4096) {
                flagPlusDisp = "b";
            }
        }

        //String hexDisp = Integer.toHexString(displacement).toUpperCase();
        flagPlusDisp += to3DigitHex(displacement);
        return flagPlusDisp;
    }

    private String getTA (String label){
        String pureOperand = label;
        if (pureOperand.startsWith("#") || pureOperand.startsWith("@"))
            pureOperand = pureOperand.substring(1);
        if (pureOperand.endsWith(",X"))
            pureOperand = pureOperand.substring(0, pureOperand.length() - 2);
        return Pass1.symbolTable.get(pureOperand);
    }

    private String to3DigitHex(int number) {
        number &= 0xFFF;
        return String.format("%03X", number);
    }

    private String handleFormat4(Instruction i){
        return "";
    }

}
