package wtf.pants.stamp.mapping;

import com.google.common.io.ByteStreams;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.exceptions.MethodNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;
import wtf.pants.stamp.mapping.obj.FieldObj;
import wtf.pants.stamp.mapping.obj.MethodObj;
import wtf.pants.stamp.asm.AccessUtil;
import wtf.pants.stamp.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * @author Spacks
 */
@SuppressWarnings("unchecked call")
public class MappingManager {

    private final ClassCollector collector;
    private final ObfuscationAssigner obfuscationHandler;

    public MappingManager(ClassCollector collector) {
        this.collector = collector;
        this.obfuscationHandler = new ObfuscationAssigner(collector);
    }

    /**
     * Adds methods from a class to another class
     * @param child Child class
     * @param parent Parent Class
     */
    private void addMethod(ClassMap child, ClassMap parent) {
        parent.getMethods().stream()
                .filter(pm -> !AccessUtil.isFinal(pm.getAccess()))
                .filter(MethodObj::isSafeMethod)
                .forEach(p -> {
                    try {
                        child.getMethodFromShort(p.getMethod());
                    } catch (MethodNotFoundException e) {
                        child.addMethod(new MethodObj(child.getClassName(), p.getMethodName(), p.getMethodDesc(), p.getAccess()));
                    }
                });
    }

    /**
     * Adds all of the non-final methods from the parent class to the child class' mappings.
     * This is so when inside one of the child class' methods later on and it calls a parent method
     * it will be able to obfuscate it with the correct name
     */
    private void addParentMethods() {
        collector.getClasses().stream()
                .filter(c -> (c.hasParent() || c.hasImplementedClasses()))
                .forEach(child -> {
                    if (child.hasParent()) {
                        try {
                            ClassMap parent = collector.getParent(child);
                            addMethod(child, parent);
                        } catch (ClassMapNotFoundException e) {
                            Log.warning("Parent class not found: %s", child.getParent());
                        }
                    }

                    if (child.hasImplementedClasses()) {
                        child.getInterfaces().forEach(inter -> {
                            try {
                                ClassMap parentClass = collector.getClassMap(inter);
                                addMethod(child, parentClass);
                            } catch (ClassMapNotFoundException e) {
                                Log.warning("Interface Class not found: %s", inter);
                            }
                        });
                    }
                });
    }

    private void mapFields(ClassNode cn, ClassMap classMap) {
        if (cn.fields == null)
            return;

        final List<FieldNode> fields = cn.fields;

        fields.forEach(field ->
                classMap.addField(new FieldObj(field.desc, field.name)));
    }

    private void mapMethods(ClassNode cn, ClassMap classMap) {
        if (cn.methods == null)
            return;

        final List<MethodNode> methods = cn.methods;

        methods.forEach(method ->
                classMap.addMethod(new MethodObj(cn.name, method.name, method.desc, method.access)));
    }

    /**
     * Maps the class file
     *
     * @param bytecode The class file's bytes
     */
    private void mapClassFile(byte[] bytecode) {
        final ClassReader cr = new ClassReader(bytecode);
        final ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        final ClassMap classMap = new ClassMap(cn.name);
        Log.log("Reading class file: %s.class", cn.name);

        if (cn.superName != null && !cn.superName.equals("java/lang/Object")) {
            Log.debug("Class has a parent: %s", cn.superName);
            classMap.setParent(cn.superName);
        }

        if (cn.interfaces != null) {
            classMap.setInterfaces(cn.interfaces);
        }

        mapMethods(cn, classMap);
        mapFields(cn, classMap);

        collector.addClass(classMap);
    }

    /**
     * This will iterate the files within the target jar and map them for obfuscation
     *
     * @param inputFile Target file
     * @throws IOException
     */
    public void mapClasses(File inputFile) throws IOException {
        final ZipFile zipFile = new ZipFile(inputFile);

        Log.info("Mapping classes...");

        zipFile.stream()
                .filter(file -> file.getName().endsWith(".class"))
                .forEach(c -> {
                    try {
                        final InputStream is = zipFile.getInputStream(c);
                        byte[] classBytecode = ByteStreams.toByteArray(is);
                        mapClassFile(classBytecode);
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        Log.info("Assigning obfuscated names...");

        addParentMethods();
        obfuscationHandler.assignObfuscatedNames();

    }

}
