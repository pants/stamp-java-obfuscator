package wtf.pants.stamp.mapping.obj;

import lombok.Getter;
import lombok.Setter;
import wtf.pants.stamp.mapping.exceptions.MethodNotFoundException;
import wtf.pants.stamp.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Spacks
 */
@Getter
@Setter
public class ClassMap {

    public final List<FieldObj> fields;
    public final List<MethodObj> methods;

    private final String className;

    private String obfClassName;
    private String parent;

    private List<String> interfaces;

    private boolean library = false;

    public ClassMap(String className) {
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<>();

        this.interfaces = new ArrayList<>();
        this.className = className;
    }

    /**
     * Gets the method from it's full path. eg:'this/is/a/pkg/Class.method()V'
     * getMethodFromShort(String) for just the method name
     *
     * @param methodId Method path
     * @return Returns the found MethodObj
     * @throws MethodNotFoundException Throws if the method has not been mapped in this class
     */
    public MethodObj getMethod(String methodId) throws MethodNotFoundException {
        Optional<MethodObj> methodObj = methods.stream().filter(m -> m.getFullMethod().equals(methodId)).findFirst();

        if (methodObj.isPresent())
            return methodObj.get();
        else
            throw new MethodNotFoundException(methodId);
    }

    /**
     * Gets a method from just the method name and desc
     * getMethod(String) for the full name, package and class
     *
     * @param methodId Method name
     * @return Returns the found MethodObj
     * @throws MethodNotFoundException Throws if the method has not been mapped in this class
     */
    public MethodObj getMethodFromShort(String methodId) throws MethodNotFoundException {
        Optional<MethodObj> methodObj = methods.stream().filter(m -> m.getMethod().equals(methodId)).findFirst();

        if (methodObj.isPresent())
            return methodObj.get();
        else
            throw new MethodNotFoundException(methodId);
    }

    public FieldObj getField(String fieldName) {
        Optional<FieldObj> fieldObj = fields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst();

        return fieldObj.orElse(null);
    }

    /**
     * Adds a mapped field to the class
     *
     * @param fieldObj FieldObj instance
     */
    public void addField(FieldObj fieldObj) {
        this.fields.add(fieldObj);
        Log.log("+ Added Field: %s", fieldObj.getFieldName());
    }

    /**
     * Adds a mapped method to the class
     *
     * @param methodObj MethodObj instance
     */
    public void addMethod(MethodObj methodObj) {
        this.methods.add(methodObj);
        Log.log("+ Added Method: %s", methodObj.getFullMethod());
    }

    public boolean isObfuscated() {
        return obfClassName != null;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasImplementedClasses() {
        return !interfaces.isEmpty();
    }

}
