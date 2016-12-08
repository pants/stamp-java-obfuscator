package wtf.pants.stamp.obfuscator.obfuscators;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.*;
import wtf.pants.stamp.mapping.ClassCollector;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.exceptions.MethodNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;
import wtf.pants.stamp.mapping.obj.MethodObj;
import wtf.pants.stamp.obfuscator.Obfuscator;
import wtf.pants.stamp.util.Log;

import java.util.List;

/**
 * @author Spacks
 */
@SuppressWarnings("unchecked")
public class ObfuscatorMethods extends Obfuscator {

    private final ClassCollector cc;

    public ObfuscatorMethods(ClassCollector cc) {
        super("Methods", 1);
        this.cc = cc;
    }

    private String getMethodId(ClassNode cn, MethodNode m) {
        return String.format("%s.%s%s", cn.name, m.name, m.desc);
    }

    @Override
    public void obfuscate(ClassReader classReader, ClassNode cn, int pass) {
        try {
            final List<MethodNode> methodNodes = cn.methods;
            final ClassMap classMap = cc.getClassMap(cn.name);

            methodNodes.forEach(method -> obfuscateMethod(classMap, cn, method));
        } catch (ClassMapNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void obfuscateMethod(ClassMap classMap, ClassNode cn, MethodNode method) {
        try {
            final String methodId = getMethodId(cn, method);
            final MethodObj methodObj = classMap.getMethod(methodId);

            Log.log("Obfuscating Method: %s", method.name);

            obfuscateInstructions(method, classMap);

            //Method name gets obfuscated after the instructions for logging purposes
            if (methodObj.isObfuscated()) {
                method.name = methodObj.getObfMethodName();
            }
        } catch (MethodNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Goes through the method's instructions for method calls and lambdas
     * @param method instance of the method we're searching
     * @param map ClassMap instance
     */
    private void obfuscateInstructions(MethodNode method, ClassMap map) {
        final InsnList array = method.instructions;

        for (int i = 0; i < array.size(); i++) {
            final AbstractInsnNode node = array.get(i);

            try {
                if (node instanceof MethodInsnNode) {
                    modifyMethodInstruction(map, method, (MethodInsnNode) node);
                } else if (node instanceof InvokeDynamicInsnNode) {
                    modifyLambdaInstruction(map, method, (InvokeDynamicInsnNode) node);
                }
            } catch (MethodNotFoundException | ClassMapNotFoundException e) {
                //TODO Debate on printing if the method is not found.
            }
        }
    }

    private void modifyMethodInstruction(ClassMap map, MethodNode method, MethodInsnNode methodNode) throws MethodNotFoundException, ClassMapNotFoundException {
        final String methodId = methodNode.owner + "." + methodNode.name + "" + methodNode.desc;

        final boolean selfMethod = methodNode.owner.equals(map.getClassName());
        final MethodObj methodMap =
                selfMethod ? map.getMethod(methodId) : cc.getClassMap(methodNode.owner).getMethod(methodId);

        if (methodMap.isObfuscated()) {
            Log.log("[%s] %s to %s", method.name, methodNode.name, methodMap.getObfMethodName());
            methodNode.name = methodMap.getObfMethodName();
        }
    }

    private void modifyLambdaInstruction(ClassMap map, MethodNode method, InvokeDynamicInsnNode in) throws MethodNotFoundException {
        if (in.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")) {
            final Handle h = ((Handle) in.bsmArgs[1]);
            final String methodId = h.getOwner() + "." + h.getName() + h.getDesc();

            final MethodObj methodObj = map.getMethod(methodId);

            if (methodObj.isObfuscated()) {
                Log.log("[%s] %s to %s", method.name, h.getName(), methodObj.getObfMethodName());

                final Handle newHandle = new Handle(h.getTag(), h.getOwner(), methodObj.getObfMethodName(), h.getDesc());
                in.bsmArgs[1] = newHandle;
            }
        }
    }
}
