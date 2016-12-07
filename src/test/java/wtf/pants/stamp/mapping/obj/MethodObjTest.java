package wtf.pants.stamp.mapping.obj;

import static org.junit.Assert.*;

import org.junit.Test;
import org.objectweb.asm.Opcodes;


/**
 * @author Spacks
 */
public class MethodObjTest {

    @Test
    public void testIsSafeMethod() throws Exception {
        //Safe method: Is not a constructor or main method
        final MethodObj safeMethod = new MethodObj("a/pkg/AClass", "testMethod", "(Ljava/lang/String;)V", Opcodes.ACC_PUBLIC);
        assertTrue(safeMethod.isSafeMethod());

        //Not safe: Method is a constructor (<init>)
        final MethodObj initMethod = new MethodObj("a/pkg/AClass", "<init>", "()V", Opcodes.ACC_PUBLIC);
        assertFalse(initMethod.isSafeMethod());

        //Not safe: Method is a main method
        final MethodObj mainMethod = new MethodObj("a/pkg/AClass", "main", "([Ljava/lang/String;)V", Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC);
        assertFalse(mainMethod.isSafeMethod());
    }
}