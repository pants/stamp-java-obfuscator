package wtf.pants.stamp.asm;

import lombok.Setter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Spacks
 */
public class ASM {

    @Setter
    private InsnList insn;

    public ASM() {}

    public ASM(InsnList insn) {
        this.insn = insn;
    }

    private int getConst(int i) {
        switch (i) {
            case 0:
                return ICONST_0;
            case 1:
                return ICONST_1;
            case 2:
                return ICONST_2;
            case 3:
                return ICONST_3;
            case 4:
                return ICONST_4;
            case 5:
                return ICONST_5;
            default:
                return -1;
        }
    }

    /**
     * Pushes the correct int to the stack.
     * 'ICONST_#' if it's less than 6
     * 'BIPUSH #' if it's -127 to -1, and 6 to 127
     * 'SIPUSH #' if it's anything else
     *
     * @param i The int to push to the stack
     */
    public void pushInt(int i) {
        final int ICONST = getConst(i);

        if (ICONST != -1) {
            insn.add(new InsnNode(ICONST));
        } else if (i > -128 && i < 128) {
            insn.add(new IntInsnNode(BIPUSH, i));
        } else {
            insn.add(new IntInsnNode(SIPUSH, i));
        }
    }

    public void dup() {
        insn.add(new InsnNode(DUP));
    }

    public void isub(){
        insn.add(new InsnNode(ISUB));
    }

    public void aastore() {
        insn.add(new InsnNode(AASTORE));
    }

    public void aaload() {
        insn.add(new InsnNode(AALOAD));
    }

    public void field(int opcode, String owner, String name, String desc) {
        insn.add(new FieldInsnNode(opcode, owner, name, desc));
    }

    public void aload(int i) {
        insn.add(new VarInsnNode(ALOAD, i));
    }


}
