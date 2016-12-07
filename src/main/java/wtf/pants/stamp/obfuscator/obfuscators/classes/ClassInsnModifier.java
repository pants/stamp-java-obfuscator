package wtf.pants.stamp.obfuscator.obfuscators.classes;

import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.*;
import wtf.pants.stamp.mapping.obj.ClassMap;

/**
 * @author Spacks
 */
@SuppressWarnings("unchecked")
public class ClassInsnModifier {

    private String obfuscateString(ClassMap classMap, String s) {
        return s.replace(classMap.getClassName(), classMap.getObfClassName());
    }

    private Handle obfuscateHandle(ClassMap c, Handle h) {
        int tag = h.getTag();
        String owner = h.getOwner();
        String name = h.getName();
        String desc = h.getDesc();

        owner = obfuscateString(c, owner);
        desc = obfuscateString(c, desc);

        return new Handle(tag, owner, name, desc);
    }

    void obfuscateMethodInsn(ClassMap c, MethodInsnNode method) {
        if (method.owner.contains(c.getClassName())) {
            method.owner = method.owner.replace(c.getClassName(), c.getObfClassName());
        }

        if (method.desc.contains(c.getClassName())) {
            method.desc = method.desc.replace(c.getClassName(), c.getObfClassName());
        }
    }

    void obfuscateFieldInsn(ClassMap c, FieldInsnNode field) {
        if (field.owner.contains(c.getClassName())) {
            field.owner = field.owner.replace(c.getClassName(), c.getObfClassName());
        }

        if (field.desc.contains(c.getClassName())) {
            field.desc = field.desc.replace(c.getClassName(), c.getObfClassName());
        }
    }

    void obfuscateTypeInsn(ClassMap c, TypeInsnNode typeInsn) {
        if (typeInsn.desc.contains(c.getClassName())) {
            typeInsn.desc = typeInsn.desc.replace(c.getClassName(), c.getObfClassName());
        }
    }

    void obfuscateFrameInsn(ClassMap c, FrameNode frameNode) {
        if (frameNode.local != null) {
            for (int i1 = 0; i1 < frameNode.local.size(); i1++) {
                Object o = frameNode.local.get(i1);
                if (o instanceof String) {
                    if (o.toString().contains(c.getClassName()))
                        frameNode.local.set(i1, obfuscateString(c, o.toString()));
                }
            }
        }

        if (frameNode.stack != null) {
            for (int j = 0; j < frameNode.stack.size(); j++) {
                Object o = frameNode.stack.get(j);
                if (o instanceof String) {
                    if (o.toString().contains(c.getClassName()))
                        frameNode.stack.set(j, obfuscateString(c, o.toString()));
                }
            }
        }
    }

    void obfuscateLambda(ClassMap c, InvokeDynamicInsnNode lambda) {
        lambda.bsm = obfuscateHandle(c, lambda.bsm);
        //TODO: Make this recursively check the args for Object[]
        for (int i1 = 0; i1 < lambda.bsmArgs.length; i1++) {
            if (lambda.bsmArgs[i1] instanceof Handle) {
                Handle handle = (Handle) lambda.bsmArgs[i1];
                lambda.bsmArgs[i1] = obfuscateHandle(c, handle);
            }
        }
    }
    
}
