package wtf.pants.stamp.mapping.obj;

import lombok.Data;

/**
 * @author Spacks
 */
@Data
public class FieldObj {

    private final String pkg, fieldName;
    private String obfPkg, obfFieldName;

    public FieldObj(String pkg, String fieldName) {
        this.pkg = pkg;
        this.fieldName = fieldName;
    }

    public String getField() {
        return pkg + "." + fieldName;
    }

    public boolean isObfuscated() {
        return obfFieldName != null;
    }

}
