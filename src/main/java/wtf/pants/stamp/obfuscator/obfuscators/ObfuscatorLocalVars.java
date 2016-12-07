package wtf.pants.stamp.obfuscator.obfuscators;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import wtf.pants.stamp.obfuscator.Obfuscator;
import wtf.pants.stamp.util.ObfUtil;

import java.util.List;

/**
 * @author Spacks
 */
@SuppressWarnings("unchecked")
public class ObfuscatorLocalVars extends Obfuscator {

    public ObfuscatorLocalVars() {
        super("Local Vars", 0);
    }

    @Override
    public void obfuscate(ClassReader classReader, ClassNode cn, int pass) {
        final List<MethodNode> list = cn.methods;
        list.forEach(m -> {
            if (m.localVariables != null) {
                List<LocalVariableNode> local = m.localVariables;
                local.stream().forEach(l -> l.name = ObfUtil.getRandomObfString());
            }
        });
    }
}
