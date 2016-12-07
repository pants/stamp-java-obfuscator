package wtf.pants.stamp.asm;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import wtf.pants.stamp.asm.ASM;

import static org.junit.Assert.*;


/**
 * @author Spacks
 */
public class ASMTest {

    @Test
    public void testPushInt() throws Exception {

        final InsnList insnList = new InsnList();
        final ASM asm = new ASM(insnList);

        //0 - 5 should all be ICONST_#
        {
            asm.pushInt(0);
            assertEquals(insnList.get(insnList.size() - 1).getOpcode(), Opcodes.ICONST_0);

            asm.pushInt(3);
            assertEquals(insnList.get(insnList.size() - 1).getOpcode(), Opcodes.ICONST_3);
        }

        //-127 - -1 and 6 - 127, should always be BIPUSH
        {
            asm.pushInt(-3);
            assertEquals(insnList.get(insnList.size() - 1).getOpcode(), Opcodes.BIPUSH);

            asm.pushInt(10);
            assertEquals(insnList.get(insnList.size() - 1).getOpcode(), Opcodes.BIPUSH);

            asm.pushInt(127);
            assertEquals(insnList.get(insnList.size() - 1).getOpcode(), Opcodes.BIPUSH);

            asm.pushInt(-127);
            assertEquals(insnList.get(insnList.size() - 1).getOpcode(), Opcodes.BIPUSH);
        }

        //Everything <= -128 and everything >= 128 should be SIPUSH
        {
            asm.pushInt(128);
            assertEquals(insnList.get(insnList.size() - 1).getOpcode(), Opcodes.SIPUSH);

            asm.pushInt(-128);
            assertEquals(insnList.get(insnList.size() - 1).getOpcode(), Opcodes.SIPUSH);
        }

    }
}