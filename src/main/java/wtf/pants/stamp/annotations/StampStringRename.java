package wtf.pants.stamp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Pants
 *
 * To use this, reference stamp as a dependency and place the annotation at the top of a method.
 * @StampStringRename("wtf/pants/something/Something.printStuff()V")
 * Above would replace strings containing "printStuff" with its obfuscated name
 * TODO: Make this more friendly to use
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface StampStringRename {

    String[] value();

}
