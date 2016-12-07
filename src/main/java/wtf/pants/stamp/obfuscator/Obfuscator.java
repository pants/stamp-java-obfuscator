package wtf.pants.stamp.obfuscator;

import lombok.Getter;
import lombok.Setter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import wtf.pants.stamp.asm.ASM;

/**
 * @author Spacks
 */
@Getter
public abstract class Obfuscator extends ASM {

    private final String name;
    //Higher the priority, the sooner it will executed
    private final int priority;

    @Setter
    @Getter
    private boolean enabled = true;

    public Obfuscator(String name) {
        this(name, 0);
    }

    public Obfuscator(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public abstract void obfuscate(ClassReader classReader, ClassNode cn, int pass);
}
