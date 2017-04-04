package wtf.pants.stamp.mapping;

import lombok.Data;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;
import wtf.pants.stamp.mapping.obj.MethodObj;

import java.util.*;

/**
 * @author Pants
 */
@Data
public class ClassCollector {

    private final List<ClassMap> classes;

    private String mainClass;

    public ClassCollector() {
        this.classes = new ArrayList<>();
    }

    /**
     * Adds a class to the collector
     *
     * @param classMap ClassMap instance
     */
    public void addClass(ClassMap classMap) {
        this.classes.add(classMap);
    }

    /**
     * Looks for a ClassMap from all the mapped classes
     *
     * @param className Class name you're looking for
     * @return Returns ClassMap
     * @throws ClassMapNotFoundException Exception thrown if className was not found
     */
    public ClassMap getClassMap(String className) throws ClassMapNotFoundException {
        final Optional<ClassMap> classMap =
                classes.stream().filter(c -> c.getClassName().equals(className)).findFirst();

        if (classMap.isPresent()) {
            return classMap.get();
        } else {
            throw new ClassMapNotFoundException();
        }
    }

    /**
     * If it has one, this will get the class' parent class
     *
     * @param classMap Mapped class
     * @return Returns parent ClassMap
     * @throws ClassMapNotFoundException Throws ClassMapNotFoundException if the parent is not mapped
     */
    public ClassMap getParent(ClassMap classMap) throws ClassMapNotFoundException {
        final String parentClassName = classMap.getParent();

        Optional<ClassMap> optional = classes.stream()
                .filter(clazz -> parentClassName.equals(clazz.getClassName()))
                .findAny();

        if (optional.isPresent())
            return optional.get();
        else
            throw new ClassMapNotFoundException(classMap.getParent());
    }

    /**
     * Tries to get the class' overridden methods by comparing the child's methods to the parent's
     *
     * @param parentClass Parent class to compare to
     * @param childClass  Child class to compare to
     * @return Returns a list of the overridden methods
     */
    public Map<MethodObj, MethodObj> getOverriddenMethods(ClassMap parentClass, ClassMap childClass) {
        final Map<MethodObj, MethodObj> methods = new HashMap<>();

        parentClass.getMethods().stream()
                .filter(MethodObj::isSafeMethod)
                .forEach(parentMethod -> {
                    for (MethodObj childMethod : childClass.getMethods()) {
                        if (childMethod.isSafeMethod() && childMethod.getMethod().equals(parentMethod.getMethod())) {
                            methods.put(parentMethod, childMethod);
                            break;
                        }
                    }
                });

        return methods;
    }

}
