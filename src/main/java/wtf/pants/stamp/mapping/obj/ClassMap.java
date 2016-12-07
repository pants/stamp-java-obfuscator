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

    public MethodObj getMethod(String methodId) throws MethodNotFoundException {
        Optional<MethodObj> methodObj = methods.stream().filter(m -> m.getFullMethod().equals(methodId)).findFirst();

        if (methodObj.isPresent())
            return methodObj.get();
        else
            throw new MethodNotFoundException(methodId);
    }

    public MethodObj getMethodFromShort(String methodId) throws MethodNotFoundException {
        Optional<MethodObj> methodObj = methods.stream().filter(m -> m.getMethod().equals(methodId)).findFirst();

        if (methodObj.isPresent())
            return methodObj.get();
        else
            throw new MethodNotFoundException(methodId);
    }

    public FieldObj getField(String fieldName) {
        Optional<FieldObj> fieldObj = fields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst();

        if (fieldObj.isPresent())
            return fieldObj.get();
        else
            return null;
    }

    public void addField(FieldObj fieldObj) {
        this.fields.add(fieldObj);
        Log.log("+ Added Field: %s", fieldObj.getFieldName());
    }

    public void addMethod(MethodObj methodObj) {
        this.methods.add(methodObj);
        Log.log("+ Added Method: %s", methodObj.getFullMethod());
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasImplementedClasses() {
        return !interfaces.isEmpty();
    }

}
