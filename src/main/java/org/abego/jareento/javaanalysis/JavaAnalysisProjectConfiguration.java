package org.abego.jareento.javaanalysis;

import java.io.File;

/**
 * A configuration for a {@link JavaAnalysisProject}.
 */
public interface JavaAnalysisProjectConfiguration {
    /**
     * Returns the name of the project.
     */
    String name();

    /**
     * Returns the Maven project directory, i.e. the directory
     * containing the {@code pom.xml} file of the project to be analysed.
     */
    File mavenProjectDirectory();

    /**
     * Returns the directories that are the roots for the Java source code to be
     * included in the JavaAnalysisProject.
     */
    File[] sourceRoots();

    /**
     * Returns the Jar files of the project to be analyzed.
     */
    File[] projectJars();

    /**
     * Returns the dependencies, i.e. the Jar files used to resolve references
     * from the code to be analyzed.
     */
    File[] dependencies();

}
