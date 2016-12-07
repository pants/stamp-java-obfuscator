package wtf.pants.stamp.mapping.exceptions;

/**
 * @author Spacks
 */
public class MethodNotFoundException extends Exception {
    public MethodNotFoundException(String message) {
        super("Method not found: " + message);
    }
}
