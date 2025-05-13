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
     private Map<String, String> literalValues = new LinkedHashMap<>(); // preserves insertion order
     static Map<String, Integer> literalSizes = new HashMap<>();//stores size in bytes
     static Map<String, String> literalAddresses = new HashMap<>();//addresses of literals

    public  void locCounter (File intFile){
        //File out_pass1 = new File("C:\\Users\\rsl_f\\OneDrive\\Desktop\\term 6\\systems programming\\SICXE\\src\\pass1_out.txt");
        File out_pass1 = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\pass1_out.txt");
        File litFile = new File("C:\\Users\\OPT\\OneDrive\\Desktop\\SICXE Project\\SICXE Assembler\\src\\LitTable.txt");
        Scanner intFileReader = null;
        PrintWriter pass1Write = null;
        PrintWriter litFileWrite = null;
        //boolean foundLTORG = false;
        List<String> pendingLiterals = new ArrayList<>();//literalls not yet put in pass 1
        try {
            intFileReader  = new Scanner(intFile);
            pass1Write = new PrintWriter(out_pass1);
            litFileWrite = new PrintWriter(litFile);
            // Write literal table header
            //litFileWrite.println("%-15s%-10s%-10s%n" + "Name" +"Value" +"Address");

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
                if (line.contains("END")) {
                    // Process ALL remaining literals before END
                    if (!pendingLiterals.isEmpty()) {
                       processPendingLiterals(pass1Write, pendingLiterals);
                    }
                    pass1Write.printf("%04X\tEND\n", locCount);
                    continue;
                }

                if(line.contains("LTORG")){
                    pass1Write.println("\t\t" + line);
                    //foundLTORG = true;
                    processPendingLiterals(pass1Write,pendingLiterals);
                    continue;
                }

                if (line.contains("=")) {
                    String literal = extractLiteralFromLine(line);
                    if(literal != null && !pendingLiterals.contains(literal)){
                        pendingLiterals.add(literal);
                    }
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
                    // parse the operand as decimal (since assembler directives use decimal numbers)
                    int decimalValue = Integer.parseInt(instruction.operand);
                    // convert to bytes (RESW = 3 bytes per word)
                    int bytesToAdvance = decimalValue * 3;
                    // advance the location counter (which is stored as hexadecimal internally)
                    locCount += bytesToAdvance;
                }
                else if (line.contains("RESB")){
                    int decimalValue = Integer.parseInt(instruction.operand);
                    locCount += decimalValue;
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
            }
             // Write the complete literal table
             writeLiteralTable(litFileWrite);
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
            if (litFileWrite != null) {
                litFileWrite.close();
            }
            symTable(out_pass1);
        }
    }

    private String extractLiteralFromLine(String line) {
        String[] parts = line.split("\\s+");
        String operand;
        if (parts.length > 2) {
            operand = parts[2];
        }else if(parts.length > 1){
            operand = parts[1];
        }else{
            operand = "";
        }
        if (operand.startsWith("=")) {
            String literal = operand.substring(1);
            if (!literalValues.containsKey(literal)) { //if not already there(new lietral)
                literalValues.put(literal, literal);//put it in list
                literalSizes.put(literal, calculateLiteralSize(literal));//calculate size
                return literal;
            }
        }
        return null;//no literal found
    }
    private int calculateLiteralSize(String literal) {
        if (literal.startsWith("C'") ) {
            return literal.substring(2, literal.length() - 1).length(); // 1 byte per char
        }
        else if (literal.startsWith("X'") ) {
            String hex = literal.substring(2, literal.length() - 1);
            return (hex.length() + 1) / 2; // 2 hex digits = 1 byte
        }
        return 3; // Default word size for numeric literals
    }

    private void processPendingLiterals(PrintWriter pass1Write, List<String> pendingLiterals) {
        // Sort literals by size (3-byte first for better alignment)
        //pendingLiterals.sort((a,b) -> Integer.compare(calculateLiteralSize(b), calculateLiteralSize(a)));

        for (String literal : pendingLiterals) {
            if (!literalAddresses.containsKey(literal)) {//if the literal not found
                int size = calculateLiteralSize(literal);//get size
                /*if (size == 3 && locCount % 3 != 0) { // Handle alignment for 3-byte literals
                    locCount += (3 - (locCount % 3));
                }*/
                String address = String.format("%04X", locCount);
                pass1Write.printf("%s\t=%s\n", address, literal);
                literalAddresses.put(literal, address);//add to list
                locCount += size;//add the size to loccount
            }
        }
        pendingLiterals.clear();//clear the list as these literals got written
    }

    private void writeLiteralTable(PrintWriter writer) {
        //header
        writer.printf("%-15s%-10s%-10s%n", "Name", "Size", "Address");
        for (String key : literalValues.keySet() ) {
            String value = literalValues.get(key);
            writer.printf("%-15s%-10d%-10s%n", "=" + value, literalSizes.get(value), literalAddresses.get(value));
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
            symFileWrite.printf("%-10s%-10s%n", "Label", "Location");

            while (pass1Reader.hasNextLine()){
                String line = pass1Reader.nextLine();
                String[] parts = line.split("\\s+");
                // counter - label - inst - operand
                if (parts.length == 4){
                    // label & counter
                    symFileWrite.printf( "%-10s%-10s%n", parts[1], parts[0]);
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
