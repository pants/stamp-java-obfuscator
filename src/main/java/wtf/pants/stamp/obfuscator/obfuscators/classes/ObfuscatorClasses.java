package wtf.pants.stamp.obfuscator.obfuscators.classes;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import wtf.pants.stamp.mapping.ClassCollector;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;
import wtf.pants.stamp.obfuscator.Obfuscator;
import wtf.pants.stamp.util.Log;

import java.util.List;

/**
 * @author Spacks
 */
@SuppressWarnings("unchecked")
public class ObfuscatorClasses extends Obfuscator {

    private final ClassCollector cc;
    private ClassInsnModifier insnHandler;

    public ObfuscatorClasses(ClassCollector cc) {
        super("Classes", 0);
        this.cc = cc;
        this.insnHandler = new ClassInsnModifier();
    }

    private String obfuscateString(ClassMap classMap, String s) {
        final String cn = classMap.getClassName();
        final String ob = classMap.getObfClassName();

        if (cn.equalsIgnoreCase(s))
            return ob;

        return s.replace(cn + ";", ob + ";")
                .replace(cn + ".", ob + ".");
    }

    private void obfuscateExceptions(ClassMap c, MethodNode methodNode) {
        for (int i = 0; i < methodNode.exceptions.size(); i++) {
            if (methodNode.exceptions.get(i) instanceof String) {
                final String exception = methodNode.exceptions.get(i).toString();

                if (exception.contains(c.getClassName())) {
                    methodNode.exceptions.set(i, c.getObfClassName());
                    Log.info("Changed exception: %s -> %s", exception, c.getObfClassName());
                }
            }
        }

        if (methodNode.tryCatchBlocks != null) {
            List<TryCatchBlockNode> tryCatchBlocks = methodNode.tryCatchBlocks;

            tryCatchBlocks.forEach(t -> {
                if (t.type != null)
                    t.type = obfuscateString(c, t.type);
            });
        }
    }

    private void obfuscateMethod(MethodNode methodNode) {
        cc.getClasses().stream()
                .filter(ClassMap::isObfuscated)
                .forEach(c -> {
                    if (methodNode.exceptions != null) {
                        obfuscateExceptions(c, methodNode);
                    }

                    if (methodNode.desc.contains(c.getClassName())) {
                        final String oldDesc = methodNode.desc;

                        methodNode.desc = obfuscateString(c, methodNode.desc);
                        Log.info("Renamed method desc: %s -> %s", oldDesc, methodNode.desc);
                    }

                    if (methodNode.localVariables != null) {
                        final List<LocalVariableNode> localVars = methodNode.localVariables;

                        localVars.stream()
                                .filter(l -> l.desc.contains(c.getClassName()))
                                .forEach(l -> l.desc = obfuscateString(c, l.desc));
                    }

                    searchMethodInsn(c, methodNode);
                });
    }

    /**
     * Search the method's instructions for Methods, Fields, Types, Frames, and Lambdas
     *
     * @param c          ClassMap instance
     * @param methodNode Target method to search
     */
    private void searchMethodInsn(ClassMap c, MethodNode methodNode) {
        final InsnList insnList = methodNode.instructions;

        for (int i = 0; i < insnList.size(); i++) {
            final AbstractInsnNode node = insnList.get(i);

            if (node instanceof MethodInsnNode)
                insnHandler.obfuscateMethodInsn(c, (MethodInsnNode) node);
            else if (node instanceof FieldInsnNode)
                insnHandler.obfuscateFieldInsn(c, (FieldInsnNode) node);
            else if (node instanceof TypeInsnNode)
                insnHandler.obfuscateTypeInsn(c, (TypeInsnNode) node);
            else if (node instanceof FrameNode)
                insnHandler.obfuscateFrameInsn(c, (FrameNode) node);
            else if (node instanceof InvokeDynamicInsnNode)
                insnHandler.obfuscateLambda(c, (InvokeDynamicInsnNode) node);
            else if (node instanceof LdcInsnNode)
                insnHandler.obfuscateLdc(c, (LdcInsnNode) node);
        }
    }

    private void modifyInterfaceNames(ClassNode cn) {
        for (int i = 0; i < cn.interfaces.size(); i++) {
            if (cn.interfaces.get(i) instanceof String) {
                try {
                    String in = (String) cn.interfaces.get(i);
                    ClassMap superClass = cc.getClassMap(in);
                    if (superClass.isObfuscated()) {
                        cn.interfaces.set(i, obfuscateString(superClass, in));
                    }
                } catch (ClassMapNotFoundException ignored) {
                }
            }
        }
    }

    private void obfuscateFields(ClassNode cn) {
        final List<FieldNode> fieldNodes = cn.fields;

        cc.getClasses().stream()
                .filter(ClassMap::isObfuscated)
                .forEach(c -> fieldNodes.forEach(field -> {
                    if (field.signature != null) {
                        field.signature = obfuscateString(c, field.signature);
                    }

                    if (field.desc.contains(c.getClassName())) {
                        field.desc = obfuscateString(c, field.desc);
                    }
                }));
    }

    @Override
    public void obfuscate(ClassReader classReader, ClassNode cn, int pass) {
        try {
            final ClassMap classMap = cc.getClassMap(cn.name);

            if (classMap.isObfuscated()) {
                //TODO: Make this a toggle between null
                cn.sourceFile = classMap.getObfClassName() + ".java";
                cn.sourceDebug = null;
                cn.name = classMap.getObfClassName();
            }

            if (cn.superName != null) {
                try {
                    ClassMap superClass = cc.getClassMap(cn.superName);
                    if (superClass.isObfuscated()) {
                        cn.superName = superClass.getObfClassName();
                        Log.log("Modified %s's extended class' name", classMap.getClassName());
                    }
                } catch (ClassMapNotFoundException ignored) {
                }
            }

            if (cn.interfaces != null) {
                modifyInterfaceNames(cn);
            }

            if (cn.fields != null) {
                obfuscateFields(cn);
            }

            final List<MethodNode> methodNodes = cn.methods;

            if (cn.methods != null) {
                methodNodes.forEach(this::obfuscateMethod);
            }
        } catch (ClassMapNotFoundException e) {
            e.printStackTrace();
        }
    }
}
