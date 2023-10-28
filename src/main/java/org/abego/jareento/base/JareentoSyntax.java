package org.abego.jareento.base;

/**
 * Strings identifying context syntax definitions, to be
 * used in @{@link javax.annotation.Syntax} annotations.
 */
public interface JareentoSyntax {
    //region File-related
    /**
     * Identifies the syntax of a file path, as used in a
     * {@link java.io.File#File(String)} call.
     */
    String FILE_PATH_SYNTAX = "FilePath";
    /**
     * Identifies the syntax of a file path to a Java file (i.e. ending with
     * ".java"), as used in a {@link java.io.File#File(String)} call.
     */
    String JAVA_FILE_PATH_SYNTAX = "FilePath:extension=java";
    /**
     * Identifies the syntax of a file path to a Class file (i.e. ending with
     * ".class"), as used in a  {@link java.io.File#File(String)} call.
     */
    String CLASS_FILE_PATH_SYNTAX = "FilePath:extension=class";
    //endregion
    //region MD5
    /**
     * Identifies the hex notation of an MD5 value.
     */
    String MD5_SYNTAX = "MD5";
    //endregion
    //region Jareento-related
    /**
     * Identifies the syntax of a <em>qualified name</em>, i.e. a non-empty
     * sequence of identifiers, separated by '.'.
     * <p>
     * The part before the last identifier (without the separating '.') is the
     * "qualifier", the last identifier the "simpleName" of the QualifiedName.
     * <p>
     * To cover some edge cases the dollar {@code '$'} and the hyphen {@code '-'}
     * may also be part of an identifier. Also, in this context an identifier may
     * start with digits or only consist of digits.
     * <p>
     * Examples: {@code java.lang.String}, {@code java.lang}, {@code JustAName}, {@code foo}, {@code package-info}
     * <p>
     */
    String QUALIFIED_NAME_SYNTAX = "QualifiedName";

    /**
     * Identifies the syntax of a <em>qualified name</em>
     * ({@linkplain JareentoSyntax#QUALIFIED_NAME_SYNTAX}) of a (Java) type.
     * <p>
     * The qualifier identifies the type's package or its outer type. 
     * Generics and Arrays are not included, but primitive types and {@code void} are.
     * <p>
     * Examples: {@code java.lang.String}, {code int}, {code void}
     * <p>
     */
    String QUALIFIED_TYPE_NAME_SYNTAX = "QualifiedTypeName";

    /**
     * Identifies the syntax of a <em>qualified name</em>,
     * ({@linkplain JareentoSyntax#QUALIFIED_NAME_SYNTAX}) of a (Java) type,
     * or an array of a (Java) type.
     * <p>
     * The qualifier identifies the type's package or its outer type. Generics are not included.
     * <p>
     * Examples: {@code java.lang.String}, {@code com.example.SomeType[][]}
     * <p>
     */
    String QUALIFIED_TYPE_OR_ARRAY_NAME_SYNTAX = "QualifiedTypeOrArrayName";

    /**
     * Identifies the syntax of <em>qualified signature of a method</em>.
     * <p>
     * The qualified signature of a (Java) method is the concatenation of
     * <ul>
     *     <li>its method identifier</li>
     *     <li>a '('</li>
     *     <li>the qualified type names ({@linkplain JareentoSyntax#QUALIFIED_TYPE_NAME_SYNTAX})
     *     of its parameters, separated by ", "</li>
     *     <li>a ')'</li>
     * </ul>
     * Example: <code>myMethod(java.lang.Object, java.lang.String)</code>
     */
    String QUALIFIED_METHOD_SIGNATURE_SYNTAX = "QualifiedMethodSignature";

    /**
     * Identifies the syntax of a <em>simple method declarator</em>.
     * <p>
     * The simple method declarator of a (Java) method is the concatenation of
     * <ul>
     *     <li>its qualified classname ({@linkplain JareentoSyntax#QUALIFIED_TYPE_NAME_SYNTAX})</li>
     *     <li>a '#'</li>
     *     <li>its qualified signature ({@linkplain JareentoSyntax#QUALIFIED_METHOD_SIGNATURE_SYNTAX})</li>
     * </ul>
     * <p>
     * Example: <code>org.example.SampleClass#myMethod(java.lang.Object, java.lang.String)</code>
     */
    String SIMPLE_METHOD_DECLARATOR_SYNTAX = "SimpleMethodDeclarator";

    /**
     * Identifies the syntax of a <em>method declarator</em>.
     * <p>
     * The method declarator of a (Java) method is the concatenation of
     * <ul>
     *     <li>its simple method declarator ({@linkplain JareentoSyntax#SIMPLE_METHOD_DECLARATOR_SYNTAX})</li>
     *     <li>a ':'</li>
     *     <li>its fully qualified return type ({@linkplain JareentoSyntax#QUALIFIED_TYPE_NAME_SYNTAX}).</li>
     * </ul>
     * Example: <code>org.example.SampleClass#myMethod(java.lang.Object, java.lang.String):java.lang.String</code>
     */
    String METHOD_DECLARATOR_SYNTAX = "MethodDeclarator";
    //endregion
}
