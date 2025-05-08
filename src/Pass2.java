import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Pass2 {

    public static List<Instruction> code = new ArrayList<>();
    public List<String> objectCodes = new ArrayList<>();
    String objCode;
    private final Map<String, String> symbolTable;
    public Pass2(Map<String, String> symbolTable) {
        this.symbolTable = symbolTable;
    }
    public String getLocationForLabel(String label) {
        return symbolTable.get(label);
    }
    // open file handle code
    public void openFiles(){
       // File pass1_out = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\pass1_out.txt");
        //File pass2_out = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\pass2_out.txt");
        File pass1_out = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass1_out.txt");
        File pass2_out = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass2_out.txt");

        handleCode(pass1_out, pass2_out);
    }

    public void handleCode(File pass1_out, File pass2_out){

        Scanner pass1Scan = null;
        PrintWriter pass2Write = null;

        try {
            pass1Scan =  new Scanner(pass1_out);
            pass2Write = new PrintWriter(pass2_out);
            pass2Write.println("loc \t\t label \t\t instr \t\t operand \t\t obj code");
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

                System.out.println(instr.Mnemonic + " " + objCode);

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
                // write in file
                pass2Write.write(instr.loc + "\t\t" + instr.label + "\t\t" + instr.Mnemonic + "\t\t" +
                        instr.operand + "\t\t" + objCode + "\n");
            }
        }
        catch(Exception e){
            System.out.println(e + "here");
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

    public String generateObjCode(Instruction i){
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

    public String handleByteDirective(String operand) {
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

    public String handleWordDirective(String operand) {
        // converts string to int
        int value = Integer.parseInt(operand);
        // formats int into hex string
        return String.format("%06X", value);
    }

    public String handleFormat2(Instruction i){
        // split operand at ,
        String[] regs = i.operand.split(",");
        String reg1 = registerCode(regs[0]);
        // if there is reg 2, include in objCode. if not, return 0
        String reg2 = regs.length > 1 ? registerCode(regs[1]) : "0";
        return i.opcode + reg1 + reg2;
    }

    public String registerCode(String reg) {
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

    /*  public String flagCode(Instruction inst){
        int n;
        int i;
        int x;
        int b;
        int p;
        int e;
        if(inst.operand.matches("#\\d+")) {
            n = 0;
            i = 1;
            x = 0;
            b = 0;
            p = 0;
            e = 0;
        }//format 4
        /*else if(inst.Mnemonic.startsWith("+") && inst.operand.startsWith("#")){
            n = 0;
            i = 1;
            x = 0;
            b = 0;
            p = 0;
            e = 1;
        }
        else if(inst.operand.startsWith("#") && Character.isDigit(inst.operand.charAt(0))) {
            if( Integer.parseInt(symbolTable.get(inst.operand)) >-2048 &&  Integer.parseInt(symbolTable.get(inst.operand)) < 2047){
                n = 0;
                i = 1;
                x = 0;
                b = 0;
                p = 1;
                e = 0;
            }
            else{
                n = 0;
                i = 1;
                x = 0;
                b = 1;
                p = 0;
                e = 0;
            }
        }
        else if(inst.operand.startsWith("@") && Character.isDigit(inst.operand.charAt(0))) {
            if(Integer.parseInt(symbolTable.get(inst.operand)) >-2048 && Integer.parseInt(symbolTable.get(inst.operand)) < 2047){
                n = 1;
                i = 0;
                x = 0;
                b = 0;
                p = 1;
                e = 0;
            }
            else{
                n = 1;
                i = 0;
                x = 0;
                b = 1;
                p = 0;
                e = 0;
            }
        }
        else if (inst.operand.matches("@\\d+")) {
            n = 1;
            i = 0;
            x = 0;
            b = 0;
            p = 0;
            e = 0;
        }/* format 4
        else if (inst.Mnemonic.startsWith("+") && inst.operand.startsWith("@")) {
            n = 1;
            i = 0;
            x = 0;
            b = 0;
            p = 0;
            e = 1;
        }
        else if (inst.Mnemonic.startsWith("+")) {
            n = 1;
            i = 1;
            x = 0;
            b = 0;
            p = 0;
            e = 1;
        }
        else if (inst.operand.contains(",X") && inst.Mnemonic.startsWith("+")) {
            n = 1;
            i = 1;
            x = 1;
            b = 0;
            p = 0;
            e = 1;
        }
        else if (inst.operand.contains(",X") && Character.isDigit(inst.operand.charAt(0))) {
            if(Integer.parseInt(symbolTable.get(inst.operand)) >-2048 &&  Integer.parseInt(symbolTable.get(inst.operand)) < 2047){
                n = 1;
                i = 1;
                x = 1;
                b = 0;
                p = 1;
                e = 0;
            }else{
                n = 1;
                i = 1;
                x = 1;
                b = 1;
                p = 0;
                e = 0;
            }
        }
        else if (inst.operand.contains(",X")) {
            n = 1;
            i = 1;
            x = 1;
            b = 0;
            p = 0;
            e = 0;
        }
        else if(Character.isDigit(inst.operand.charAt(0))){
            if(Integer.parseInt(symbolTable.get(inst.operand)) >-2048 &&  Integer.parseInt(symbolTable.get(inst.operand)) < 2047){
                n = 1;
                i = 1;
                x = 0;
                b = 0;
                p = 1;
                e = 0;
            }else {
                n = 1;
                i = 1;
                x = 0;
                b = 1;
                p = 0;
                e = 0;
            }
        }
        else{
            n = 1;
            i = 1;
            x = 0;
            b = 0;
            p = 0;
            e = 0;
        }
        String combined = "" + n + i + x + b + p + e;
        return combined;
    }*/

    public String flagCode(Instruction inst) {
        int n = 1;
        int i = 1;
        int x = 0;
        int b = 0;
        int p = 0;
        int e = 0;
        if (inst.operand.startsWith("#")){
            n = 0;
            i = 1;
            x = 0;
            e = inst.Mnemonic.startsWith("+") ? 1 : 0;
            if(e == 1) handleFormat4(inst);
            String valuePart = inst.operand.substring(1);
            if(valuePart.matches("\\d+")){
                p = 0;
                b = 0;
            }
            else{
                String loc = symbolTable.get(valuePart);
                System.out.println("Symbol table entry for " + valuePart + ": " + loc);

                if(loc != null){
                    if(Integer.parseInt(loc,16) >= -2048 &&  Integer.parseInt(loc,16) <= 2047){
                        p = 1;
                        b = 0;
                    }else{
                        p = 0;
                        b = 1;
                    }
                 }
//                else {
//                    p = 0;
//                    b = 0;
//                }
            }
        }
        else if(inst.operand.contains(",X")){
            n = 1;
            i = 1;
            x = 1;  // Indexed mode
            e = inst.Mnemonic.startsWith("+") ? 1 : 0;
            if(e == 1) handleFormat4(inst);
            String baseOperand = inst.operand.split(",")[0];
            if(baseOperand.matches("\\d+")){
                p = 0;
                b = 0;
                /*if(Integer.parseInt(baseOperand) >= -2048 &&  Integer.parseInt(baseOperand) <= 2047){
                    p = 1;
                    b = 0;
                }else{
                    p = 0;
                    b = 1;
                }*/
            }else {
                String loc = symbolTable.get(baseOperand);
                System.out.println("Symbol table entry for " + baseOperand + ": " + loc);
                if(loc != null){
                    if(Integer.parseInt(loc,16) >= -2048 &&  Integer.parseInt(loc,16) <= 2047){
                        p = 1;
                        b = 0;
                    }else {
                        p = 0;
                        b = 1;
                    }
                }
//                else{
//                    p = 0;
//                    b = 0;
//                }
            }
        } else if (inst.Mnemonic.startsWith("@")) {
            n = 1;
            i = 0;
            x = 0;
            e = inst.Mnemonic.startsWith("+") ? 1 : 0;
            if(e == 1) handleFormat4(inst);
            String symbol = inst.operand;

            if(symbol.matches("\\d+")){
                p = 0;
                b = 0;
               /* if(Integer.parseInt(symbol) >= -2048 &&  Integer.parseInt(symbol) <= 2047){
                    p = 1;
                    b = 0;
                }*/
            }else {
                String loc = symbolTable.get(symbol);
                System.out.println("Symbol table entry for " + symbol + ": " + loc);
                if(loc != null){
                    if(Integer.parseInt(loc,16) >= -2048 &&  Integer.parseInt(loc,16) <= 2047){
                        p = 1;
                        b = 0;
                    }else {
                        p = 0;
                        b = 1;
                    }
                }
//                else {
//                    p = 0;
//                    b = 0;
//                }
            }

        } else if (inst.Mnemonic.startsWith("+")) {
            n = 1;
            i = 1;
            x = 0;
            b = 0;
            p = 0;
            e = 1;
            handleFormat4(inst);
        } else if(Integer.parseInt(inst.operand) >= -2048 &&  Integer.parseInt(inst.operand) <= 2047){
            n = 1;
            i = 1;
            x = 0;
            b = 0;
            p = 1;
            e = 0;
        }else{
            n = 1;
            i = 1;
            x = 0;
            b = 1;
            p = 0;
            e = 0;
        }
        return "" + n + i + x + b + p + e;
    }

    public String handleFormat3(Instruction i){
        File pass1Out = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass1_out.txt");
        Scanner in = null;
        try {
            in = new Scanner(pass1Out);
            Pass1 pass = new Pass1();
            int base = pass.baseAddress;
            String disp = dis(i,Integer.parseInt(i.loc, 16),base);
            // 1. Convert hex opcode to binary
            String binaryOpcode = hexToBinary(i.opcode);
            // 2. Remove last 2 bits
            if (binaryOpcode.length() >= 2) {
                binaryOpcode = binaryOpcode.substring(0, binaryOpcode.length() - 2);
            }
            // 3. Pad to 6 bits (for format 3 instructions)
            binaryOpcode = String.format("%6s", binaryOpcode).replace(' ', '0');
            // 4. Get flags
            String flags = flagCode(i);
            // 5. Combine opcode (6 bits) + flags (6 bits)
            String combinedBinary = binaryOpcode + flags;
            // 6. Convert to hex (3 hex digits)
            String combinedHex = binaryToHex(combinedBinary);
            // 7. Format displacement to 3 hex digits
            String formattedDisp = formatDisplacement(disp,i);

            // 8. Combine everything (3 + 3 hex digits)
            return combinedHex + formattedDisp;
        }catch (Exception e) {
            return e.toString();
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
    // Helper method to convert hex to binary
    private String hexToBinary(String hex) {
        // Remove any 'x' or 'h' suffix if present
        hex = hex.replaceAll("[xXhH]", "");

        // Convert to binary with leading zeros
        return String.format("%8s",
                        Integer.toBinaryString(Integer.parseInt(hex, 16)))
                .replace(' ', '0');
    }
    // Helper method to format displacement
    private String formatDisplacement(String disp,Instruction inst) {
        try {
            // If it's already a hex number
            if (disp.matches("[0-9A-Fa-f]+")) {
                int value = Integer.parseInt(disp, 16);
                return String.format("%03X", value & 0xFFF); // Ensure 12-bit value
            }
            // If it's a decimal number
            else if (disp.matches("-?\\d+")) {
                int value = Integer.parseInt(disp);
                return String.format("%03X", value & 0xFFF); // Ensure 12-bit value
            }
            // For labels or other cases
            else if(symbolTable.containsKey(disp)) {
                int symbolAddress = Integer.parseInt(symbolTable.get(disp));
                int displacement = symbolAddress - (Integer.parseInt(inst.loc)); // PC-relative addressing
                return String.format("%03X", displacement & 0xFFF);
                //return "000"; // Placeholder - you'll need symbol resolution here
            }
            else {
                return "000";
            }
        } catch (NumberFormatException e) {
            return "000"; // Fallback value
        }
    }
    // Your existing binaryToHex method
    private String binaryToHex(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return String.format("%03X", decimal);
    }
    public String calDisp(Instruction i, Scanner in) {
        String flag = flagCode(i);
        String disp = "";
        int x;
        String objcode;
        if (flag == "110001") {
            disp = i.loc;
            return disp;
        }else if (flag.equals("110010")) {  // PC-relative
                String label = i.operand;
                String targetLoc = symbolTable.get(label);
                if (targetLoc == null) {
                    System.out.println("Undefined label: " + label);
                }
                int target = Integer.parseInt(targetLoc, 16);
                int pc = Integer.parseInt(i.loc, 16) + 3;  // PC points to next instruction
                disp = Integer.toHexString(target - pc);
            } else if (flag == "110100") {
            x = Integer.parseInt(symbolTable.get(i.operand)) + Integer.parseInt(i.base);
            disp = String.valueOf(x);
            return disp;
        } /*else if (flag == "111000") {
            x =  Integer.parseInt(symbolTable.get(i.operand)) - Integer.parseInt();
            disp = String.valueOf(x);
            return disp;
        }*/ else if (flag == "111001") {
            disp = i.loc;
            return disp;
        } else if (flag == "111010") {
            x = Integer.parseInt(symbolTable.get(i.operand)) + Integer.parseInt(in.nextLine());
            disp = String.valueOf(x);
            return disp;
        } else if (flag == "111100") {
            x = Integer.parseInt(symbolTable.get(i.operand)) - Integer.parseInt(i.base);
            disp = String.valueOf(x);
            return disp;
        } /*else if (flag == "100000") {
                disp = ;
                return disp;
            }*/ else if (flag == "100001") {
            disp = i.loc;
            return disp;
        } else if (flag == "100010") {
            x = Integer.parseInt(symbolTable.get(i.operand)) - Integer.parseInt(in.nextLine());
            disp = String.valueOf(x);
            return disp;
        } else if (flag == "100100") {
            x = Integer.parseInt(symbolTable.get(i.operand)) + Integer.parseInt(i.base);
            disp = String.valueOf(x);
            return disp;
        } else if (flag == "010000") {
            if (Character.isDigit(i.operand.charAt(0))) {
                disp = i.loc;
                return disp;
            } else {
                disp = i.operand;
                return disp;
            }
        } else if (flag == "010001") {
            disp = i.loc;
            return disp;
        } else if (flag == "010010") {
            x = Integer.parseInt(symbolTable.get(i.operand)) - Integer.parseInt(in.nextLine());
            disp = String.valueOf(x);
            return disp;
        } else if (flag == "010100") {
            x = Integer.parseInt(symbolTable.get(i.operand)) + Integer.parseInt(i.base);
            disp = String.valueOf(x);
            return disp;
        }
        return disp;
    }
    public String calDis(Instruction i, Scanner in) {
        String flag = flagCode(i);
        String disp = "";
        int x;
        String objcode;
        try{
            if (flag == "110001") {
                disp = i.loc;
            } else if (flag == "110010") {
                x = Integer.parseInt(symbolTable.getOrDefault(i.operand,"0"),16);
                int pc =  Integer.parseInt(in.nextLine(), 16);
                disp = Integer.toHexString(x - pc);
            } else if (flag == "110100") {
                x = Integer.parseInt(symbolTable.getOrDefault(i.operand,"0"),16);
                int base = Integer.parseInt(in.nextLine(), 16);
                disp = Integer.toHexString(x - base);
            } else if (flag == "111000") {
                String baseOperand = i.operand.split(",")[0];
                if (baseOperand.matches("\\d+")) {
                    disp = baseOperand;
                } else {
                    disp = symbolTable.getOrDefault(baseOperand, "0");
                }
            } else if (flag == "111001") {
                disp = i.loc;

            } else if (flag == "111010") {
                x = Integer.parseInt(symbolTable.getOrDefault(i.operand, "0"), 16);
                int pc = Integer.parseInt(in.nextLine(), 16);
                disp = Integer.toHexString(x - pc);
            } else if (flag == "111100") {
                x = Integer.parseInt(symbolTable.getOrDefault(i.operand, "0"), 16);
                int base = Integer.parseInt(i.base, 16);
                disp = Integer.toHexString(x - base);
            } /*else if (flag == "100000") {
                    disp = ;
                    return disp;
            }*/
            else if (flag == "100001") {
        disp = i.loc;

    } else if (flag == "100010") {
        x =  Integer.parseInt(symbolTable.getOrDefault(i.operand, "0"), 16);
        int pc = Integer.parseInt(in.nextLine(), 16);
        disp = Integer.toHexString(x - pc);

    } else if (flag == "100100") {
        x =  Integer.parseInt(symbolTable.getOrDefault(i.operand, "0"), 16);
        int base = Integer.parseInt(i.base, 16);
        disp = Integer.toHexString(x - base);

    } else if (flag == "010000") {
        if (i.operand.matches("\\d+")) {
            disp = i.operand;
        } else {
            disp = symbolTable.getOrDefault(i.operand.substring(1), "0");
        }
    }
            else {
        // Default case - should ideally never happen
        disp = "0";
    }/*else if (flag == "010001") {
                disp = i.loc;
                return disp;
            } else if (flag == "010010") {
                x = Integer.parseInt(symbolTable.get(i.operand)) - Integer.parseInt(in.nextLine());
                disp = String.valueOf(x);
                return disp;
            } else if (flag == "010100") {
                x = Integer.parseInt(symbolTable.get(i.operand)) + Integer.parseInt(i.base);
                disp = String.valueOf(x);
                return disp;
            }*/
            return disp;
}
        catch (NumberFormatException e) {
        return "000";  // Fallback value
        }
        }

    public String dis(Instruction inst, int currentAddress, int baseAddress) {
        // Default displacement for errors or no displacement needed
        final String DEFAULT_DISP = "000";
        // 1. First check for null Instruction
        if (inst == null) {
            return DEFAULT_DISP;
        }

        // 2. Then check for RSUB (using the already-parsed opcode)
        if (inst.opcode.equals("4C")) {
            return DEFAULT_DISP;
        }

        // 2. Handle instructions without operands
        if (inst.operand == null || inst.operand.trim().isEmpty()) {
            return DEFAULT_DISP;
        }

        String flag = flagCode(inst);
        String operand = inst.operand.trim();
        String disp = DEFAULT_DISP;

        try {
            // 3. Handle format 4 (direct addressing)
            if ("110001".equals(flag)) {
                return inst.loc != null ? inst.loc : DEFAULT_DISP;
            }

            // 4. Extract clean symbol name (handles all cases)
            String symbol = cleanSymbol(operand);

            // 5. Handle numeric immediate values (e.g., #100)
            if (operand.startsWith("#") && symbol.matches("\\d+")) {
                int value = Integer.parseInt(symbol);
                return String.format("%03X", value & 0xFFF);
            }

            // 6. Get target address - handle missing symbols gracefully
            String targetAddrStr = symbolTable.get(symbol);
            if (targetAddrStr == null) {
                System.err.printf("Warning: Symbol '%s' not found at %04X%n",
                        symbol, currentAddress);
                return DEFAULT_DISP;
            }

            int targetAddr;
            try {
                targetAddr = Integer.parseInt(targetAddrStr, 16);
            } catch (NumberFormatException e) {
                System.err.printf("Invalid address format for symbol '%s' at %04X%n",
                        symbol, currentAddress);
                return DEFAULT_DISP;
            }

            // 7. Calculate displacement based on addressing mode
            if ("110010".equals(flag) || "111010".equals(flag) || "100010".equals(flag)) {
                // PC-relative addressing
                int displacement = targetAddr - (currentAddress + 3); // Format 3 length

                if (displacement >= -2048 && displacement <= 2047) {
                    disp = String.format("%03X", displacement & 0xFFF);
                }
                // Fallback to base-relative if possible
                else if (baseAddress != -1) {
                    int baseDisp = targetAddr - baseAddress;
                    if (baseDisp >= 0 && baseDisp <= 4095) {
                        disp = String.format("%03X", baseDisp & 0xFFF);
                    }
                }
            }
            else if ("111100".equals(flag) || "100100".equals(flag)) {
                // Base-relative addressing
                if (baseAddress != -1) {
                    int displacement = targetAddr - baseAddress;
                    if (displacement >= 0 && displacement <= 4095) {
                        disp = String.format("%03X", displacement & 0xFFF);
                    }
                }
            }

        } catch (Exception e) {
            System.err.printf("Error processing instruction at %04X: %s - %s%n",
                    currentAddress, inst, e.getMessage());
        }

        return disp;
    }

    // Helper method to extract clean symbol name from operand
    private String cleanSymbol(String operand) {
        if (operand == null) return "";

        // Handle indexed addressing
        String symbol = operand.split(",")[0].trim();

        // Remove addressing prefixes (@ or #)
        if (symbol.startsWith("@") || symbol.startsWith("#")) {
            symbol = symbol.substring(1).trim();
        }

        return symbol;
    }
    public String handleFormat4(Instruction i){
        return "";
    }
}
