package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.Many;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.Syntax;
import java.util.stream.Stream;

import static org.abego.jareento.base.JareentoSyntax.METHOD_DECLARATOR_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.METHOD_SIGNATURE_SYNTAX;
import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX;

/**
 * Contains 0 or more JavaMethodDeclarator instances.
 */
public interface JavaMethodDeclarators extends Many<JavaMethodDeclarator> {

    /**
     * Returns a {@link Stream} with the texts of the method declarators in this instance.
     */
    @Syntax(METHOD_DECLARATOR_SYNTAX)
    Stream<String> textStream();

    /**
     * Returns {@code true} if this instance contains at least one method of the given
     * class (or some inner class of that class), or {@code false} otherwise.
     */
    boolean containsMethodOfClass(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);

    /**
     * Returns {@code true} if this instance contains at least one method of the 
     * given class (or some inner class of that class) with the requested
     * {@code signature}, or {@code false} otherwise.
     */
    boolean containsMethodOfClassWithSignature(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName,
            @Syntax(METHOD_SIGNATURE_SYNTAX) String signature);

    /**
     * Returns the text of the MethodDeclarator of the method in the given {@code typeName}
     * that has the requested {@code signature} or, null if no such method
     * exists.
     */
    @Nullable
    @Syntax(METHOD_DECLARATOR_SYNTAX)
    String getMethodDeclaratorTextOfMethodOfClassWithSignatureOrNull(
            @Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName,
            @Syntax(METHOD_SIGNATURE_SYNTAX) String signature);

}
