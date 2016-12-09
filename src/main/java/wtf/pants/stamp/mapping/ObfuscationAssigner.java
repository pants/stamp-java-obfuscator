package wtf.pants.stamp.mapping;

import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;
import wtf.pants.stamp.mapping.obj.MethodObj;
import wtf.pants.stamp.util.Log;
import wtf.pants.stamp.util.ObfUtil;

/**
 * @author Spacks
 */
class ObfuscationAssigner {

    private final ClassCollector collector;

    ObfuscationAssigner(ClassCollector cc) {
        this.collector = cc;
    }

    /**
     * Assigns obfuscated names to the overridden methods
     *
     * @param parentClass Parent Class
     * @param classObj    Child Class
     */
    private void obfuscateParentChild(ClassMap parentClass, ClassMap classObj) {
        collector.getOverriddenMethods(parentClass, classObj).forEach((parentMethod, childMethod) -> {
            if (!parentMethod.isObfuscated()) {
                parentMethod.setObfMethodName(ObfUtil.getRandomObfString());
            }

            childMethod.setObfMethodName(parentMethod.getObfMethodName());
            childMethod.setObfuscationDisable(false);

            Log.log("Obfuscated Overridden Method: %s. Renamed to: %s", childMethod.getMethodName(), childMethod.getObfMethodName());
        });
    }

    /**
     * Obfuscates the implemented class' overridden methods. If the implemented class is not found it will disable the
     * target class from being obfuscated. TODO: Remove the second sentence when libraries are done
     *
     * @param classMap Instance of ClassMap
     */
    private void obfuscateInterfaceMethods(ClassMap classMap) {
        classMap.getInterfaces().forEach(inter -> {
            try {
                ClassMap interfaceClass = collector.getClassMap(inter);
                obfuscateParentChild(interfaceClass, classMap);
            } catch (ClassMapNotFoundException e) {
                classMap.methods.stream()
                        .filter(methodObj -> !methodObj.isObfuscated())
                        .forEach(m -> m.setObfuscationDisable(true));
                Log.log("Interface class not found. Parent: %s", classMap.getClassName(), classMap.getParent());
                e.printStackTrace();
            }
        });
    }

    private boolean isExcluded(String clazz, String[] exclusions) {
        for (String exclusion : exclusions) {
            if (clazz.toLowerCase().startsWith(exclusion.toLowerCase())) {
                Log.info("Excluding %s", clazz);
                return true;
            }
        }

        return false;
    }

    /**
     * Goes through the mapped classes and assigns an obfuscated name to each class for actually obfuscating
     *
     * @param exclusions String array of paths/classes to exclude from assigning obfuscated names
     */
    void assignObfuscatedNames(String[] exclusions) {
        collector.getClasses().stream()
                .filter(cm -> !isExcluded(cm.getClassName(), exclusions))
                .forEach(classObj -> {
                    if (classObj.hasParent()) {
                        try {
                            ClassMap parentClass = collector.getParent(classObj);
                            obfuscateParentChild(parentClass, classObj);
                        } catch (ClassMapNotFoundException e) {
                            classObj.methods.stream()
                                    .filter(methodObj -> !methodObj.isObfuscated())
                                    .forEach(m -> m.setObfuscationDisable(true));

                            Log.log("%s's parent class not found. Parent: %s", classObj.getClassName(), classObj.getParent());
                        }
                    }

                    if (classObj.hasImplementedClasses()) {
                        obfuscateInterfaceMethods(classObj);
                    }

                    classObj.getMethods().stream()
                            .filter(m -> !m.isObfuscationDisable())
                            .filter(m -> !m.isObfuscated())
                            .filter(MethodObj::isSafeMethod)
                            .forEach(m -> {
                                m.setObfMethodName(ObfUtil.getRandomObfString());
                                Log.log("Method: %s -> %s", m.getMethodName(), m.getObfMethodName());
                            });

                    classObj.getFields().stream()
                            .filter(f -> !f.isObfuscated())
                            .forEach(f -> {
                                f.setObfFieldName(ObfUtil.getRandomObfString());
                                Log.log("Field: %s -> %s", f.getFieldName(), f.getObfFieldName());
                            });

                    classObj.setObfClassName(ObfUtil.getRandomObfString());
                });
    }

}
