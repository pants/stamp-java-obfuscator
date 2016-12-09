package wtf.pants.stamp.obfuscator;

import lombok.Getter;
import lombok.Setter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import wtf.pants.stamp.asm.ASM;
import wtf.pants.stamp.mapping.ClassCollector;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;

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

    protected boolean isClassObfuscated(ClassCollector collector, ClassNode cn){
        try {
            ClassMap classMap = collector.getClassMap(cn.name);

            return classMap.isObfuscated();
        } catch (ClassMapNotFoundException ex) {
            //This will be thrown when the class has already been obfuscated
            return true;
        }
    }

    public abstract void obfuscate(ClassReader classReader, ClassNode cn, int pass);
}
