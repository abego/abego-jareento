package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.JareentoSyntax;

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

    //endregion
    //region Class-related

    JavaClasses getClasses();

    boolean hasClassWithName(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);

    JavaClass getClassWithName(@Syntax(QUALIFIED_TYPE_NAME_SYNTAX) String classname);
    
    JavaClasses getAllSubClasses(JavaClasses classes);

    JavaClasses getAllSubClassesAndClasses(JavaClasses classes);


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
