package org.abego.jareento.javaanalysis;

import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.Syntax;
import java.util.stream.Stream;

import static org.abego.jareento.base.JareentoSyntax.METHOD_DECLARATOR_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_METHOD_SIGNATURE_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX;

/**
 * Contains 0 or more JavaMethodDeclarator instances.
 */
public interface JavaMethodDeclarators extends Iterable<JavaMethodDeclarator> {
    
    /**
     * Returns the number of method declarators in this instance.
     */
    int getSize();

    /**
     * Returns {@code true} when this instance contains no elements,
     * {@code true} otherwise.
     */
    default boolean isEmpty() {
        return getSize() == 0;
    }

    /**
     * Returns a {@link Stream} with the {@link JavaMethodDeclarator}s in 
     * this instance.
     */
    Stream<JavaMethodDeclarator> stream();

    /**
     * Returns a {@link Stream} with the texts of the method declarators in this instance.
     */
    @Syntax(METHOD_DECLARATOR_SYNTAX)
    Stream<String> textStream();

    /**
     * Returns true if this set contains at least one method of the given
     * class (or some inner class of that class).
     */
    boolean containsMethodOfClass(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    /**
     * Returns {@code true} when a method exists in the class with the given
     * {@code classname} with the requested {@code signature} 
     * or {@code false} if no such method exists.
     */
    boolean containsMethodOfClassAndSignature(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname,
            @Syntax(QUALIFIED_METHOD_SIGNATURE_SYNTAX) String signature);

    /**
     * Returns the text of the MethodDeclarator of the method in the given {@code type}
     * that has the requested {@code signature} or, null if no such method
     * exists.
     */
    @Nullable
    @Syntax(METHOD_DECLARATOR_SYNTAX)
    String methodDeclaratorTextOfMethodOfTypeWithSignatureOrNull(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String type,
            @Syntax(QUALIFIED_METHOD_SIGNATURE_SYNTAX) String signature);

}
