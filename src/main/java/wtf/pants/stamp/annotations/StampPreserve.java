package wtf.pants.stamp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Pants
 *
 * Currently only works for methods
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface StampPreserve {
}
