package wtf.pants.stamp.obfuscator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import wtf.pants.stamp.Stamp;
import wtf.pants.stamp.obfuscator.obfuscators.*;
import wtf.pants.stamp.obfuscator.obfuscators.classes.ObfuscatorClasses;
import wtf.pants.stamp.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Spacks
 */
public class ObfuscatorManager {

    private List<Obfuscator> obfuscatorList;

    public ObfuscatorManager(Stamp stamp) {
        this.obfuscatorList = new ArrayList<>();
        this.obfuscatorList.add(new ObfuscatorMethods(stamp.getCollector()));
        this.obfuscatorList.add(new ObfuscatorFields(stamp.getCollector()));
        this.obfuscatorList.add(new ObfuscatorClasses(stamp.getCollector()));
        this.obfuscatorList.add(new ObfuscatorStrings(stamp.getCollector()));
        this.obfuscatorList.add(new ObfuscatorLocalVars(stamp.getCollector()));
    }

    public byte[] obfuscate(ClassReader cr, ClassNode cn) {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cr.accept(cn, 0);
        obfuscatorList.stream()
                .sorted((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority())) //o1.get, o2.get
                .filter(Obfuscator::isEnabled)
                .forEach(o -> o.obfuscate(cr, cn, 0));

        Log.debug("Accepting class: %s", cn.name);
        cn.accept(cw);
        Log.debug("Accepted class");

        return cw.toByteArray();
    }

    /**
     * Disables an obfuscation type.
     *
     * @param name The name of the obfuscation type to disable
     */
    public void disableObfuscation(String name) {
        obfuscatorList.stream().filter(o -> o.getName().equalsIgnoreCase(name)).forEach(o -> o.setEnabled(false));
    }
}
