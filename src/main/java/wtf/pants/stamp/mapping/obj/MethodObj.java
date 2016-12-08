package wtf.pants.stamp.mapping.obj;

import lombok.Data;

import static wtf.pants.stamp.asm.AccessUtil.isStatic;

/**
 * @author Spacks
 */
@Data
public class MethodObj {

    private String obfPkg, obfMethodName, obfMethodDesc;

    private final String pkg, methodName, methodDesc;
    private final int access;

    private boolean obfuscationDisable = false;

    public MethodObj(String pkg, String methodName, String methodDesc, int access) {
        this.pkg = pkg;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.access = access;
    }

    /**
     * Gets the full method name. The package, the method name and desc
     *
     * @return Returns the full method path and desc "wtf/pants/stamp/mapping/obj/MethodObj.getFullMethod()V"
     */
    public String getFullMethod() {
        return pkg + "." + methodName + methodDesc;
    }

    /**
     * Gets the method name and desc
     *
     * @return Returns the method name and desc eg "getMethod()V"
     */
    public String getMethod() {
        return methodName + methodDesc;
    }

    /**
     * Check to see if the method has had an obfuscated name assigned to it
     *
     * @return Returns true if the method has an obfuscated name set
     */
    public boolean isObfuscated() {
        return obfMethodName != null;
    }

    /**
     * Checks if the method is 'safe'. If the method is a constructor or main method it should not be obfuscated
     * and deemed unsafe for obfuscation.
     *
     * @return Returns true if the method is not a constructor or main method
     */
    public boolean isSafeMethod() {
        return !(methodName.startsWith("<") //<init>, <clinit>
                || (methodName.equals("main") && methodDesc.equals("([Ljava/lang/String;)V") && isStatic(access)));
    }

}
