package org.abego.jareento.base;

import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.Syntax;
import java.util.stream.Stream;

import static org.abego.jareento.base.JareentoSyntax.FULL_METHOD_DECLARATOR_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_METHOD_SIGNATURE_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX;

/**
 * A set of Java methods.
 */
public interface JavaMethodSet {
    /**
     * Returns the number of methods in this object.
     */
    int getSize();

    /**
     * Returns a Stream of FullMethodDeclarators of the methods in this object.
     */
    @Syntax(FULL_METHOD_DECLARATOR_SYNTAX)
    Stream<String> declaratorStream();

    /**
     * Returns the FullMethodDeclarator of the method in the given {@code type}
     * that has the requested {@code signature} or, null if no such method
     * exists.
     */
    @Nullable
    @Syntax(FULL_METHOD_DECLARATOR_SYNTAX)
    String fullMethodDeclaratorOfMethodOfTypeWithSignatureOrNull(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String type,
            @Syntax(QUALIFIED_METHOD_SIGNATURE_SYNTAX) String signature);

    /**
     * Returns {@code true} when a method exists in the given {@code type}
     * that has the requested {@code signature} or {@code false} if no such
     * method exists.
     */
    boolean containsMethodOfTypeWithSignature(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String type,
            @Syntax(QUALIFIED_METHOD_SIGNATURE_SYNTAX) String signature);

    /**
     * Returns true if this set contains at least one method of the given
     * class (or some inner class of that class).
     */
    boolean containsMethodOfClass(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);
}
