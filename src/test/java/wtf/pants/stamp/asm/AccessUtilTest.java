package wtf.pants.stamp.asm;

import static org.junit.Assert.*;
import org.junit.Test;

import static org.objectweb.asm.Opcodes.*;


/**
 * @author Pants
 */
public class AccessUtilTest {

    private int public_static_final = ACC_PUBLIC + ACC_STATIC + ACC_FINAL;
    private int public_access = ACC_PUBLIC;

    @Test
    public void testIsFinal() throws Exception {
        //Access is final
        assertTrue(AccessUtil.isFinal(public_static_final));

        //Access is not final
        assertFalse(AccessUtil.isFinal(public_access));
    }

    @Test
    public void testIsStatic() throws Exception {
        //Access is static
        assertTrue(AccessUtil.isStatic(public_static_final));

        //Access is not static
        assertFalse(AccessUtil.isStatic(public_access));
    }
}