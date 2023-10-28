package org.abego.jareento.javaanalysis.internal;

import org.abego.commons.javalang.JavaLangUtil;
import org.abego.jareento.javaanalysis.JavaAnalysisProjectConfiguration;
import org.abego.jareento.shared.commons.maven.MavenUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

class JavaAnalysisProjectConfigurationImpl implements JavaAnalysisProjectConfiguration {
    private final String name;
    private final File mavenProjectDirectory;
    private final File[] projectJars;
    private final File[] dependencies;
    private final File[] sourceRoots;

    private JavaAnalysisProjectConfigurationImpl(
            String name,
            File mavenProjectDirectory,
            File[] projectJars,
            File[] dependencies,
            File[] sourceRoots) {

        this.name = name;
        this.mavenProjectDirectory = mavenProjectDirectory;
        this.projectJars = projectJars;
        this.dependencies = dependencies;
        this.sourceRoots = sourceRoots;
    }

    public static JavaAnalysisProjectConfiguration newJavaAnalysisProjectConfiguration(
            String name,
            File mavenProjectDirectory,
            File[] sourceRoots,
            File @Nullable [] projectJars,
            File @Nullable [] dependencies) {

        if (!JavaLangUtil.isJavaIdentifier(name)) {
            throw new IllegalArgumentException("Not a valid name: " + name);
        }

        File[] projectJarFiles = projectJars != null
                ? projectJars
                : MavenUtil.jarFilesInTargetOfMavenProject(mavenProjectDirectory);
        File[] dependencyJars = dependencies != null
                ? dependencies
                : MavenUtil.classpathJarsFromMavenProject(mavenProjectDirectory);
        return new JavaAnalysisProjectConfigurationImpl(
                name, mavenProjectDirectory, projectJarFiles, dependencyJars, sourceRoots);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public File getMavenProjectDirectory() {
        return mavenProjectDirectory;
    }

    @Override
    public File[] getProjectJars() {
        return projectJars;
    }

    @Override
    public File[] getDependencies() {
        return dependencies;
    }

    @Override
    public File[] getSourceRoots() {
        return sourceRoots;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaAnalysisProjectConfigurationImpl that = (JavaAnalysisProjectConfigurationImpl) o;
        return mavenProjectDirectory.equals(that.mavenProjectDirectory) && Arrays.equals(projectJars, that.projectJars) && Arrays.equals(dependencies, that.dependencies) && Arrays.equals(sourceRoots, that.sourceRoots);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(mavenProjectDirectory);
        result = 31 * result + Arrays.hashCode(projectJars);
        result = 31 * result + Arrays.hashCode(dependencies);
        result = 31 * result + Arrays.hashCode(sourceRoots);
        return result;
    }
}
