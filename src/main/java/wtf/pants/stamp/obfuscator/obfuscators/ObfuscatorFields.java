package wtf.pants.stamp.obfuscator.obfuscators;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import wtf.pants.stamp.mapping.ClassCollector;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.obj.FieldObj;
import wtf.pants.stamp.obfuscator.Obfuscator;
import wtf.pants.stamp.util.Log;

import java.util.List;

/**
 * @author Spacks
 */
@SuppressWarnings("unchecked")
public class ObfuscatorFields extends Obfuscator {

    private final ClassCollector cc;

    public ObfuscatorFields(ClassCollector cc) {
        super("Fields", 1);
        this.cc = cc;
    }

    private void searchInstructions(ClassNode cn, MethodNode methodNode) {
        if (methodNode.instructions == null) {
            return;
        }

        final InsnList insnList = methodNode.instructions;

        for (int i = 0; i < insnList.size(); i++) {
            AbstractInsnNode node = insnList.get(i);

            if (node instanceof FieldInsnNode) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
                try {
                    FieldObj fieldObj = cc.getClassMap(cn.name).getField(fieldInsnNode.name);
                    if (fieldObj != null) {
                        fieldInsnNode.name = fieldObj.getObfFieldName();
                    }
                } catch (ClassMapNotFoundException e) {
                    Log.error("Class not found...? %s", cn.name);
                }
            }
        }
    }

    @Override
    public void obfuscate(ClassReader classReader, ClassNode cn, int pass) {
        if (cn.fields != null) {
            final List<FieldNode> list = cn.fields;

            list.forEach(fieldNode -> {
                try {
                    FieldObj fieldObj = cc.getClassMap(cn.name).getField(fieldNode.name);
                    if (fieldObj != null) {
                        Log.log("[field] Obfuscated %s -> %s", fieldObj.getObfFieldName(), fieldNode.name);
                        fieldNode.name = fieldObj.getObfFieldName();
                    }
                } catch (ClassMapNotFoundException e) {
                    Log.error("Class not found...? %s", cn.name);
                }
            });
        }

        if (cn.methods != null) {
            final List<MethodNode> methodNodes = cn.methods;
            methodNodes.forEach(methodNode -> searchInstructions(cn, methodNode));
        }
    }
}
