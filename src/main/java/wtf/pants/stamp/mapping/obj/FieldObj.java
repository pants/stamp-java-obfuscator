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

    /**
     * Check to see if the method has had an obfuscated name assigned to it
     *
     * @return Returns true if the method has an obfuscated name set
     */
    public boolean isObfuscated() {
        return obfFieldName != null;
    }

}
