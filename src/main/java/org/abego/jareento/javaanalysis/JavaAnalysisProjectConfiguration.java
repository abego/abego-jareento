package org.abego.jareento.javaanalysis;

import org.eclipse.jdt.annotation.Nullable;

import java.io.File;

/**
 * A configuration for a {@link JavaAnalysisProject}.
 */
public interface JavaAnalysisProjectConfiguration {
    /**
     * Returns the name of the project.
     */
    String getName();

    /**
     * Returns the Maven project directory, i.e. the directory
     * containing the {@code pom.xml} file of the project to be analysed, or 
     * {@code null} when no Maven project directory is defined.
     */
    @Nullable
    File getMavenProjectDirectory();

    /**
     * Returns the directories that are the roots for the Java source code to be
     * included in the JavaAnalysisProject.
     */
    File[] getSourceRoots();

    /**
     * Returns the Jar files of the project to be analyzed.
     */
    File[] getProjectJars();

    /**
     * Returns the dependencies, i.e. the Jar files used to resolve references
     * from the code to be analyzed.
     */
    File[] getDependencies();

}
