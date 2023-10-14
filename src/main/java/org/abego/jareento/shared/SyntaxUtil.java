package org.abego.jareento.shared;

import javax.annotation.Syntax;

import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_NAME_SYNTAX;

public final class SyntaxUtil {

    /**
     * Returns the qualifier of the {@code qualifiedName} or an empty string if
     * the {@code qualifiedName} has no qualifier.
     */
    public static String qualifier(@Syntax(QUALIFIED_NAME_SYNTAX) String qualifiedName) {
        int i = qualifiedName.lastIndexOf('.');
        return (i > 0) ? qualifiedName.substring(0, i) : "";
    }

    /**
     * Returns the simpleName of the {@code qualifiedName}.
     */
    public static String simpleName(@Syntax(QUALIFIED_NAME_SYNTAX) String qualifiedName) {
        int i = qualifiedName.lastIndexOf('.');
        return (i > 0) ? qualifiedName.substring(i + 1) : qualifiedName;
    }

    /**
     * Splits the {@code qualifiedName} into its qualifier and simple name and
     * return them in an array (qualifier in [0], simple name in [1]).
     * <p>
     * When the qualified name has no qualifier [0] is the empty string.
     */
    public static String[] qualifierAndSimpleName(@Syntax(QUALIFIED_NAME_SYNTAX) String qualifiedName) {
        int i = qualifiedName.lastIndexOf('.');
        return i > 0
                ? new String[]{qualifiedName.substring(0, i), qualifiedName.substring(i + 1)}
                : new String[]{"", qualifiedName};
    }
}
