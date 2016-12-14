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
     * @param exclusions  Array of excluded classes
     */
    private void obfuscateParentChild(ClassMap parentClass, ClassMap classObj, String[] exclusions) {
        collector.getOverriddenMethods(parentClass, classObj).forEach((parentMethod, childMethod) -> {
            if (!isExcluded(parentClass.getClassName(), exclusions)) {
                if (!parentMethod.isObfuscated()) {
                    parentMethod.setObfMethodName(ObfUtil.getRandomObfString());
                }

                childMethod.setObfMethodName(parentMethod.getObfMethodName());
            } else {
                childMethod.setObfuscationDisable(true);
            }

            Log.log("Obfuscated Overridden Method: %s. Renamed to: %s", childMethod.getMethodName(), childMethod.getObfMethodName());
        });
    }

    /**
     * Obfuscates the implemented class' overridden methods. If the implemented class is not found it will disable the
     * target class from being obfuscated. TODO: Remove the second sentence when libraries are done
     *
     * @param classMap   Instance of ClassMap
     * @param exclusions Array of excluded classes
     */
    private void obfuscateInterfaceMethods(ClassMap classMap, String[] exclusions) {
        classMap.getInterfaces().forEach(inter -> {
            try {
                ClassMap interfaceClass = collector.getClassMap(inter);
                obfuscateParentChild(interfaceClass, classMap, exclusions);
            } catch (ClassMapNotFoundException e) {
                classMap.methods.stream()
                        .filter(methodObj -> !methodObj.isObfuscated())
                        .forEach(m -> m.setObfuscationDisable(true));
                Log.log("Interface class not found. Parent: %s", classMap.getClassName(), classMap.getParent());
                e.printStackTrace();
            }
        });
    }

    /**
     * Obfuscates the paren'ts methods (recursively, if the parent has a parent)
     *
     * @param classObj   Instance of ClassMap
     * @param exclusions Array of excluded classes
     */
    private void obfuscateParentMethods(ClassMap classObj, String[] exclusions) {
        try {
            ClassMap parentClass = collector.getParent(classObj);

            //Recursively add parent methods
            if (parentClass.hasParent()) {
                obfuscateParentMethods(parentClass, exclusions);

                if (parentClass.hasImplementedClasses()) {
                    obfuscateInterfaceMethods(parentClass, exclusions);
                }
            }

            obfuscateParentChild(parentClass, classObj, exclusions);
        } catch (ClassMapNotFoundException e) {
            classObj.methods.stream()
                    .filter(methodObj -> !methodObj.isObfuscated())
                    .forEach(m -> m.setObfuscationDisable(true));

            Log.log("%s's parent class not found. Parent: %s", classObj.getClassName(), classObj.getParent());
        }
    }

    private boolean isExcluded(String clazz, String[] exclusions) {
        if (exclusions == null)
            return false;

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
                        obfuscateParentMethods(classObj, exclusions);
                    }

                    if (classObj.hasImplementedClasses()) {
                        obfuscateInterfaceMethods(classObj, exclusions);
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
