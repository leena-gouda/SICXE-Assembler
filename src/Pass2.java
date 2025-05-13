import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.*;


public class Pass2 {

    public static List<Instruction> code = new ArrayList<>();
    public List<String> objectCodes = new ArrayList<>();
    String objCode;
    String PC;
    public Pass2() { }
    private Map<String,String> literalAddresses;
    private Map<String,Integer> literalSizes;


    public void openFiles(){
        //File pass1_out = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\pass1_out.txt");
        //File pass2_out = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\pass2_out.txt");
        File pass1_out = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass1_out.txt");
        File pass2_out = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass2_out.txt");

        handleCode(pass1_out, pass2_out);
    }

    private void handleCode(File pass1_out, File pass2_out){

        Scanner pass1Scan = null;
        PrintWriter pass2Write = null;
        literalAddresses = Pass1.literalAddresses;
        literalSizes = Pass1.literalSizes;

        try {
            pass1Scan = new Scanner(pass1_out);
            pass2Write = new PrintWriter(pass2_out);

            pass2Write.println("loc\t\tlabel\t\tinstr\t\toperand\t\t\tobj code");

            Instruction i;
            while (pass1Scan.hasNextLine()) {

                i = new Instruction();
                String line = pass1Scan.nextLine();
                String[] parts = line.split("\\s+");

                // assign parts of each line to each instr object

                // for start dir
                if (parts.length == 3 && parts[1].equals("START")) {
                    i.label = parts[0];
                    i.Mnemonic = parts[1];
                    i.operand = parts[2];
                }
                // if there's no label
                else if (parts.length == 3) {
                    i.loc = parts[0];
                    i.Mnemonic = parts[1];
                    i.operand = parts[2];
                }
                // if there's a label
                else if (parts.length == 4) {
                    i.loc = parts[0];
                    i.label = parts[1];
                    i.Mnemonic = parts[2];
                    i.operand = parts[3];
                }
                // for base dir
                else if (line.contains("BASE")) {
                    i.Mnemonic = parts[0];
                    i.operand = parts[1];
                    i.base = i.loc;
                }
                // for f1 instructions
                else if (parts.length == 2) {
                    i.loc = parts[0];
                    i.Mnemonic = parts[1];
                }

                // assign format and opcode to each inst obj
                i.format = Instruction.findFormat(i.Mnemonic);
                i.opcode = Instruction.findOpcode(i.Mnemonic);

                // add each instr obj to code
                if(!i.Mnemonic.equals("BASE")){
                    Pass2.code.add(i);
                }


            }

            for (Instruction instr : code) {
                // go to generateObjCode method
                objCode = generateObjCode(instr);

                // write in file
                if (instr.label == null) {
                    instr.label = "";
                }
                if (instr.loc == null) {
                    instr.loc = "";
                }
                if (instr.operand == null) {
                    instr.operand = "";
                }

                    objectCodes.add(objCode);
                    instr.objCode = objCode;


                String loc = instr.loc != null ? instr.loc : "";
                String label = instr.label != null ? instr.label : "";
                String mnemonic = instr.Mnemonic != null ? instr.Mnemonic : "";
                String operand = instr.operand != null ? instr.operand : "";
                // special handling for directives
                if (mnemonic.equals("BASE")) {
                    pass2Write.printf("%-8s%-16s%-8s%-16s%n", "", "", "BASE", operand);
                    continue;
                }
                // format with tabs and fixed spacing
                pass2Write.printf("%-8s%-16s%-8s%-16s%-8s%n", loc, label, mnemonic, operand, objCode);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
      finally {
            if (pass1Scan != null) {
                pass1Scan.close();
            }
            if (pass2Write != null) {
                pass2Write.close();
            }

            HTMERecords(pass2_out);
        }
    }

    private String generateObjCode(Instruction i){
        // no obj code for these directives
        if (i.Mnemonic.equals("START") || i.Mnemonic.equals("END")
                || i.Mnemonic.equals("RESW") || i.Mnemonic.equals("RESB")) {
            return " ";
        }
        else if (i.Mnemonic.equals("BASE"))
            return "";
        else if (i.Mnemonic.equals("BYTE")) {
            return handleByteDirective(i.operand);
        }
        else if (i.Mnemonic.equals("WORD")) {
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
        }else if(i.operand != null && i.operand.startsWith("=")){
            return handleLiteralOperand(i);
        }else if(i.Mnemonic.startsWith("=")){
            return handleByteDirective(i.Mnemonic);
        }
        // if regular instruction
        else {
            return switch (i.format) {
                case 1 -> i.opcode; // f1 -> opcode only
                case 2 -> handleFormat2(i);
                case 3 -> handleFormat3(i);
                case 4 -> handleFormat4(i);
                default -> "Invalid Instruction: format not found";
            };
        }
    }

    private String handleLiteralOperand(Instruction i) {
        String literal = i.operand.substring(1); // Remove '='

        // Check if literal exists
        if (!literalAddresses.containsKey(literal)) {
            return "UNDEFINED LITERAL";
        }
        // Handle different instruction formats
        switch (i.format) {
            case 3: return handleFormat3(i);
            case 4: return handleFormat4(i);
            default: return "INVALID FORMAT FOR LITERAL";
        }
    }

    private String handleByteDirective(String operand) {
        String result = "";
        String pureOperand = operand.substring(2, operand.length() - 1);
        if (operand.startsWith("=C'")) {
            // Extract the string inside quotes
            String str = operand.substring(3, operand.length() - 1);
            StringBuilder objcode = new StringBuilder();
            for (Byte b : str.getBytes()) {
                String hex = Integer.toHexString(b & 0xFF).toUpperCase();
                if (hex.length() == 1) {
                    objcode.append('0');
                }
                objcode.append(hex);
            }
            result = objcode.toString();
        } else if (operand.startsWith("=X'")) {
            // Extract hex string directly
            result = operand.substring(3, operand.length() - 1).toUpperCase();
        }// remove X or C prefix
        else if (operand.startsWith("C")) {
            StringBuilder objCode = new StringBuilder();
            // converts string to byte array and loops through it
            for (byte b : pureOperand.getBytes()) {
                // converts byte ASCII to hex string and uppercase
                String hex = Integer.toHexString(b & 0xFF).toUpperCase();
                if (hex.length() == 1) {
                    objCode.append('0');
                }
                // concatenates to string builder
                objCode.append(hex);
            }
            return objCode.toString();
        }else{
                result = pureOperand; // fallback if needed
        }
        return result;
    }

    private String handleWordDirective(String operand) {
        if (operand == null)
            return "Operand is null";
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
        String lit = "";
        char e = '0';
        char p = '0';
        char b = '0';

        if (i.Mnemonic.equals("RSUB")){
            return "4F0000";
        }

        if(i.operand.isEmpty()){
            return "Invalid Instruction: no operand";
        }

        if ((i.operand.startsWith("#") || i.operand.startsWith("@")) && i.operand.endsWith(",X")){
            return "Invalid instruction: cannot be indexed as well as indirect/immediate";
        }

        String opcode = handleOpCode(i);

        String NIX = setNIX(i);

        // if immediate, check if operand is a number. if yes, convert to hex and place in obj code
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

        // to remove first character (p/b)
        String disp = addMode.substring(1);
        objCode = binaryToHex(opcode + NIX + b + p + e) + disp;
        return objCode;
    }

    private String hexToBinary(String hex) {
        // remove any 'x' or 'h' suffix if present
        hex = hex.replaceAll("[xXhH]", "");
        // convert to binary with leading zeros
        return String.format("%8s", Integer.toBinaryString(Integer.parseInt(hex, 16))).replace(' ', '0');
    }

    private String binaryToHex(String binary) {
        // parse binary (12 bits) and format as 3 uppercase hex digits
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
        // get index of instruction
        int j = Pass2.code.indexOf(inst);

        // if next instructions isn't BASE, load into PC the location of the next instruction
        if (!Pass2.code.get(j + 1).Mnemonic.equals("BASE"))
            PC = Pass2.code.get(j + 1).loc;
        // otherwise, the location of the instruction after the BASE directive
        else PC = Pass2.code.get(j + 2).loc;
        int PCInt = Integer.parseInt(PC, 16);

        String TA = getTA(inst.operand);
        if (TA == null || !TA.matches("[0-9A-Fa-f]+")) {
            // Handle case where operand is unresolved or invalid
            System.err.println("Invalid target address for operand: " + inst.operand);
            return "Invalid Target Address";
        }
            int TAInt = Integer.parseInt(TA, 16);

        // calculate displacement, if within range of PC relative, set p = 1
        int displacement = TAInt - PCInt;
        if (displacement >= -2048 && displacement <= 2047) {
            flagPlusDisp = "p";
        }
        // otherwise, calculate displacement with respect to base and if within range, set b = 1
        else {
            int base  = Integer.parseInt(Pass1.baseAddress, 16);
            displacement = TAInt - base;
            if (displacement >= 0 && displacement <= 4096) {
                flagPlusDisp = "b";
            }
        }

        // return string containing flag (p/b) and displacement
        flagPlusDisp += to3DigitHex(displacement);
        return flagPlusDisp;
    }

    private String getTA (String label){
        String pureOperand = label;
        if(label.startsWith("=")){
            String literal = label.substring(1);
            return literalAddresses.getOrDefault(literal, null);
        }
        // if it starts with # or @, remove to search for label in symbol table
        if (pureOperand.startsWith("#") || pureOperand.startsWith("@"))
            pureOperand = pureOperand.substring(1);

        // same idea for ,X
        if (pureOperand.endsWith(",X"))
            pureOperand = pureOperand.substring(0, pureOperand.length() - 2);

        String loc =  Pass1.symbolTable.get(pureOperand);

        if(loc == null)
            return "Label not found";
        else return loc;
    }

    private String to3DigitHex(int number) {
        number &= 0xFFF;
        return String.format("%03X", number);
    }

    private String to5DigitHex(int number) {
        number &= 0xFFFFF;
        return String.format("%05X", number);
    }

    private String handleFormat4(Instruction i) {
        if(i.operand == null || i.operand.isEmpty()){
            return "Invalid Instruction: no operand";
        }

        if ((i.operand.startsWith("#") || i.operand.startsWith("@")) && i.operand.endsWith(",X")){
            return "Invalid instruction: cannot be indexed as well as indirect/immediate";
        }
        char e = '1';
        char p = '0';
        char b = '0';
        String NIX = setNIX(i);
        String opcode = handleOpCode(i);

        if ((NIX.equals("010") || NIX.equals("100")) && i.operand.substring(1).matches("\\d+")){
            return binaryToHex(opcode + NIX + b + p + e) + to5DigitHex(Integer.parseInt(i.operand.substring(1)));
        }

        String address = getTA(i.operand);

        if (address != null) {
            return binaryToHex(opcode + NIX + b + p + e) + to5DigitHex(Integer.parseInt(address, 16));
        } else {
            return "label not found in symbol table";
        }
    }

    private void HTMERecords(File pass2out) {
        File htmeFile= new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\HTME.txt");
        //File htmeFile= new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\HTME.txt");
        Scanner pass2Reader = null;
        PrintWriter htmeWriter = null;
        try {
            pass2Reader = new Scanner(pass2out);
            htmeWriter = new PrintWriter(htmeFile);
            String H = HRecord(pass2out);
            htmeWriter.println(H);
            String T = TRecord(pass2out);
            htmeWriter.print(T);
            String M = "";
            for (Instruction i : code) {
                if (i.operand != null && i.operand.startsWith("=")) {
                    System.out.println("Skipping instruction with literal operand: " + i);
                } else if (i.operand == null) {
                    System.err.println("Skipping instruction with null operand: " + i);
                }
                String currentAddress = i.loc;
                if(i.Mnemonic.startsWith("+") && !i.operand.startsWith("#")){
                    M = MRecord(currentAddress);
                    htmeWriter.println(M);
                }
            }
            String E = ERecord(pass2out);
            htmeWriter.println(E);
        } catch (FileNotFoundException e) {
            System.err.printf("Error opening file '%s'%n", pass2out.getAbsolutePath());
        }
        finally {
            if (pass2Reader != null) {
                pass2Reader.close();
            }
            if (htmeWriter != null) {
                htmeWriter.close();
            }
        }
    }

    private int StartingAddress(File pass2out) {
        Scanner pass2Reader = null;
        try {
            int startAddress = 0;
            pass2Reader = new Scanner(pass2out);
            // Skip header line if exists
            if (pass2Reader.hasNextLine()) {
                String line = pass2Reader.nextLine();
                if (line.trim().startsWith("loc") || line.trim().startsWith("address")) {
                    if (pass2Reader.hasNextLine()) {
                        line = pass2Reader.nextLine();
                    }
                }

                // Parse first actual code line
                String[] parts = line.trim().split("\\s+");

                if (parts.length >= 3 && parts[1].equals("START")) {
                    startAddress = Integer.parseInt(parts[2], 16);  // Address is third element
                }
            }
            return startAddress;
        }catch (Exception e){
            return 0;
        }
    }

    private String HRecord(File pass2Out) {
        Scanner pass2Reader = null;
        try {
            pass2Reader = new Scanner(pass2Out);
            String name = "";
            int startingAddress = StartingAddress(pass2Out);
            int endAddress = 0;

            // Skip header line if exists
            if (pass2Reader.hasNextLine()) {
                String line = pass2Reader.nextLine();
                if (line.trim().startsWith("loc") || line.trim().startsWith("address")) {
                    if (pass2Reader.hasNextLine()) {
                        line = pass2Reader.nextLine();
                    }
                }

                // Parse first actual code line
                String[] parts = line.trim().split("\\s+");

                if (parts.length >= 3 && parts[1].equals("START")) {
                    name = parts[0];  // Program name is first element
                    //startingAddress = Integer.parseInt(parts[2], 16);  // Address is third element
                }
            }

            // Find END directive
            while (pass2Reader.hasNextLine()) {
                String line = pass2Reader.nextLine().trim();
                String[] parts = line.split("\\s+");

                if (parts.length >= 2 && parts[1].equals("END")) {
                    // Get address from first column of END line
                    if (!parts[0].isEmpty()) {
                        endAddress = Integer.parseInt(parts[0], 16);
                    }
                    break;
                }
            }

            // Handle name padding
            if (name.isEmpty()) {
                name = "XXXXXX";
            } else {
                name = name.length() > 6 ? name.substring(0, 6)
                        : String.format("%-6s", name).replace(' ', 'X');
            }

            // Calculate program length
            int programLength = endAddress - startingAddress;

            // Format the H record
            return "H" + name.toUpperCase() + String.format("%06X", startingAddress) + String.format("%06X", programLength);

        } catch (Exception e) {
            e.printStackTrace();
            return "HXXXXXX000000000000";
        } finally {
            if (pass2Reader != null) {
                pass2Reader.close();
            }
        }
    }

    private String TRecord(File pass2out) {
        StringBuilder finalRecord = new StringBuilder();
        int currentAddress = StartingAddress(pass2out);
        List<String> objcodes = new ArrayList<>();

        int length = 0;

        for (Instruction i  : code) {

            if(i.Mnemonic.equals("END"))
                continue;

            int j = Pass2.code.indexOf(i);
            int byteSize;

            if(!i.objCode.isEmpty() && !i.objCode.equals(" ")) {
                byteSize = i.objCode.length() / 2; // 2 chars = 1 byte
            }
            else byteSize = 0;

            // space means RESB or RESW so start a new t
            if (i.objCode.equals(" ")) {
                //if there is previous objcodes in list print them
                if (!objcodes.isEmpty()) {
                    finalRecord.append(buildTRecord(currentAddress - length , objcodes)); // currentAddress - length means the starting address of t
                    objcodes.clear(); //remove the objcodes from list to not repeat them when we start a new t
                    length = 0; //same for length
                }
                // Skip RESB or RESW but go to next address
                currentAddress = Integer.parseInt(code.get(j + 1).loc, 16);
                continue;
            }

            // if the current size greater than 1E (30) make a new t
            if (length + byteSize > 0x1E) {
                //if there is previous objcodes in list print them
                if (!objcodes.isEmpty()) {
                    finalRecord.append(buildTRecord(currentAddress - length , objcodes)); // currentAddress - length means the starting address of t
                    objcodes.clear();//remove the objcodes from list to not repeat them when we start a new t
                    length = 0;//same for length
                }
                // Start new record with current objCode
                objcodes.add(i.objCode); //put current obj code in list
                length = byteSize;    // length contains the number of bytes
                currentAddress = Integer.parseInt(code.get(j + 1).loc, 16); //add the number of bytes on current address to move forward with addresses based on size of instruction(format)
            }
            else {//if theres no special cases
                objcodes.add(i.objCode);
                length += byteSize;
                currentAddress = Integer.parseInt(code.get(j + 1).loc, 16);
            }
        }

        // add the remaining objcodes
        if (!objcodes.isEmpty()) {
            finalRecord.append(buildTRecord(currentAddress - length , objcodes));
        }

        return finalRecord.toString();
    }

    private String buildTRecord(int startAddress, List<String> objcodes) {
        int totalBytes = 0;
        StringBuilder mergedCodes = new StringBuilder(); // Stores all objcodes
        for (String code : objcodes) {
            totalBytes += code.length() / 2;  // Calculate totalbytes
            mergedCodes.append(code);
        }
        return String.format("T%06X%02X%s%n", startAddress, totalBytes, mergedCodes);
    }

    private String MRecord(String currentAddress) {
        int address = Integer.parseInt(currentAddress,16) + 1;
        double halfByte = 20/(8 * 0.5);
        int hb = (int) halfByte;
        return  "M" + String.format("%06X", address) +String.format("%02X", hb);
    }

    private String ERecord(File pass2out) {
            int startAddress = StartingAddress(pass2out);
            return "E" + String.format("%06X", startAddress);
    }

    private String handleOpCode(Instruction i) {
        // convert hexadecimal opcode to binary
        String opcode = hexToBinary(i.opcode);

        // remove last two bits of opcode
        opcode = opcode.substring(0, opcode.length() - 2);

        return opcode;
    }

}

