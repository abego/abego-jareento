package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.JareentoSyntax;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisFiles;

import javax.annotation.Syntax;
import java.io.File;

import static org.abego.jareento.base.JareentoSyntax.QUALIFIED_TYPE_NAME_SYNTAX;

/**
 * A collection of Java language elements (classes, methods, ...) and their
 * relations, to be used for (static) program analysis.
 */
public interface JavaAnalysisProject {

    //region Project-related
    File[] getSourceRoots();

    File[] getDependencies();
    
    JavaAnalysisFiles getJavaAnalysisFiles();

    //endregion
    //region Type-related

    JavaTypes getTypes();

    boolean hasTypeWithName(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);

    JavaType getTypeWithName(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String typeName);
    
    JavaTypes getAllSubTypes(JavaTypes types);

    JavaTypes getAllSubTypesAndTypes(JavaTypes types);


    //endregion
    //region Method-related

    /**
     * Returns all methods contained in this project.
     */
    JavaMethods getMethods();
    
    JavaMethod getMethodWithMethodDeclarator(
            @Syntax(JareentoSyntax.METHOD_DECLARATOR_SYNTAX) String methodDeclaratorText);


    //endregion
    //region MethodCall-related
    JavaMethodCalls getMethodCalls();
    //endregion
}
