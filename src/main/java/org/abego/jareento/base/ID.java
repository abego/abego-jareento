package org.abego.jareento.base;

import javax.annotation.meta.TypeQualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Typically used on String elements that identify objects, i.e. that hold
 * an `ID`.
 */
@Documented
@TypeQualifier(applicableTo = CharSequence.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ID {
    /**
     * The type name of the instance the ID identifies.
     * <p>
     * A simple or qualified Java type name may be used, or even the name of
     * a concept not covered by a concrete Java type.
     * <p>
     * An empty value ({@code @ID("")} indicates an element is used as an ID
     * but does not give a specific type. However, when {@code @ID("")} is used
     * on a method that returns the object's id (typically {@code id()}) the
     * type is the type containing the method (avoid repeating the type).
     */
    String value();
}
