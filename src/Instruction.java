import java.util.ArrayList;
import java.util.List;

public class Instruction {
    String opcode;
    int format;
    String Mnemonic;
    String operand = null;
    String label;
    String loc;
    String base;
    String objCode;

    static List<Instruction> instructions = new ArrayList<>();

    public Instruction(String Mnemonic, int format, String opcode) {
        this.opcode = opcode;
        this.format = format;
        this.Mnemonic = Mnemonic;
    }

    public Instruction() {

    }

    public static int findFormat (String Mnemonic) {
        if (Mnemonic.startsWith("+"))
            return 4;
        for (Instruction i : instructions) {
            if (i.Mnemonic.equals(Mnemonic)) {
                return i.format;
            }
        }
        return 0;
    }

    public static String findOpcode (String Mnemonic) {
        // remove to find opcode in list
        if (Mnemonic.startsWith("+")) {
            Mnemonic = Mnemonic.substring(1);
        }
        for (Instruction i : instructions) {
            if (i.Mnemonic.equals(Mnemonic)) {
                return i.opcode;
            }
        }
        return "";
    }
}
