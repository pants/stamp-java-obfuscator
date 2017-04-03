package wtf.pants.stamp.asm;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @author Pants
 */
public class AccessUtil {

    public static boolean isFinal(int access) {
        return (access & ACC_FINAL) != 0;
    }

    public static boolean isStatic(int access) {
        return (access & ACC_STATIC) != 0;
    }

}
