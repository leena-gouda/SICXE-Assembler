import java.util.ArrayList;
import java.util.List;

public class Instruction {
    String opcode;
    int format;
    String Mnemonic;
    String operand;
    String label;
    static List<Instruction> instructions = new ArrayList<>();
    public Instruction(String Mnemonic, int format, String opcode) {
        this.opcode = opcode;
        this.format = format;
        this.Mnemonic = Mnemonic;
    }

    public Instruction() {

    }

    public static int findFormat (String Mnemonic) {
        int f = 0;
        for (Instruction i : instructions) {
            if (i.Mnemonic.equals(Mnemonic)) {
                f = i.format;
            }
        }
        return f;
    }

    public static String findOpcode (String Mnemonic) {
        String op = "";
        for (Instruction i : instructions) {
            if (i.Mnemonic == Mnemonic) {
                op = i.opcode;
            }
        }
        return op;
    }

}
