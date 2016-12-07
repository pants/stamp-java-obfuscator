package wtf.pants.stamp.mapping;

import lombok.Getter;
import wtf.pants.stamp.mapping.exceptions.ClassMapNotFoundException;
import wtf.pants.stamp.mapping.obj.ClassMap;
import wtf.pants.stamp.mapping.obj.MethodObj;

import java.util.*;

/**
 * @author Spacks
 */
public class ClassCollector {

    @Getter
    private final List<ClassMap> classes;

    private final List<String> classesNotFound;

    public ClassCollector() {
        this.classes = new ArrayList<>();
        this.classesNotFound = new ArrayList<>();
    }

    public void addClass(ClassMap classMap) {
        this.classes.add(classMap);
    }

    public ClassMap getClassMap(String className) throws ClassMapNotFoundException {
        //TODO: Maybe get rid of this, in the end will it actually make a difference?
        //In cases of large amounts of classes, hopefully will avoid going through many
        if (classesNotFound.contains(className))
            throw new ClassMapNotFoundException();

        Optional<ClassMap> classMap =
                classes.stream().filter(c -> c.getClassName().equals(className)).findFirst();

        if (classMap.isPresent()) {
            return classMap.get();
        } else {
            classesNotFound.add(className);
            throw new ClassMapNotFoundException();
        }
    }

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
