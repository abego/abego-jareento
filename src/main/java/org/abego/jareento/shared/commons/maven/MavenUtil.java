package org.abego.jareento.shared.commons.maven;

import org.abego.commons.io.FileUtil;
import org.abego.commons.lang.ArrayUtil;
import org.abego.commons.util.PropertiesGroup;
import org.eclipse.jdt.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import static org.abego.commons.io.FileUtil.filesInDirectoryAndDeeper;
import static org.abego.commons.util.PropertiesGroup.newPropertiesGroup;
import static org.abego.jareento.shared.commons.maven.MavenUtil.JarFileType.JAVADOC_JAR;
import static org.abego.jareento.shared.commons.maven.MavenUtil.JarFileType.MAIN_JAR;
import static org.abego.jareento.shared.commons.maven.MavenUtil.JarFileType.SOURCES_JAR;
import static org.abego.jareento.shared.commons.maven.MavenUtil.JarFileType.TESTS_JAR;

public class MavenUtil {
    private static final PropertiesGroup propertiesGroup =
            newPropertiesGroup("abego.maven");

    public static String runMavenCommand(String argument, File mavenProjectDirectory) {
        return runMavenCommand(new String[]{argument}, mavenProjectDirectory);
    }

    public static String runMavenCommand(String[] arguments, File mavenProjectDirectory) {
        checkIsMavenProjectDirectory(mavenProjectDirectory);

        StringBuilder errors = new StringBuilder();
        StringBuilder result = new StringBuilder();
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(propertiesGroup.getProperty("abego.maven.tool", "mvn"));
            Collections.addAll(cmd, arguments);
            cmd.add("-f");
            cmd.add(mavenProjectDirectory.getAbsolutePath());

            Process process = Runtime.getRuntime()
                    .exec(cmd.toArray(new String[0]));
            InputStream stdOut = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdOut));
            String lineText = reader.readLine();
            while (lineText != null) {
                if (lineText.startsWith("[ERROR]")) {
                    errors.append(lineText);
                    errors.append("\n");
                } else if (!lineText.startsWith("[INFO]")) {
                    if (result.length() > 0) {
                        result.append("\n");
                    }
                    result.append(lineText);
                }
                lineText = reader.readLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(
                    String.format("Error when running 'mvm %s' for '%s'",
                            String.join(" ", arguments),
                            mavenProjectDirectory.getAbsolutePath()),
                    e);
        }

        if (errors.length() > 0) {
            throw new IllegalStateException(
                    String.format("Error when running 'mvm %s' for '%s': %n%s",
                            String.join(" ", arguments),
                            mavenProjectDirectory.getAbsolutePath(),
                            errors));
        }
        return result.toString();
    }

    public static File[] classpathJarsFromMavenProject(
            @Nullable File mavenProjectDirectory) {
        if (mavenProjectDirectory == null) {
            return new File[0];
        }
        File[] files = classpathOfMavenProject(mavenProjectDirectory);
        return Arrays.stream(files)
                .filter(f -> f.getName().endsWith(".jar"))
                .toArray(File[]::new);
    }

    public static File sourceDirectoryFromMavenProject(
            File mavenProjectDirectory) {
        // for now return the standard directory
        return new File(new File(new File(mavenProjectDirectory, 
                "src"), "main"), "java");
    }

    public enum JarFileType {
        /**
         * Contains the "main" code of a module.
         * <p>
         * By default, any file not recognized as a TEST_JAR, SOURCES_JAR or
         * JAVADOC_JAR is considered to be a MAIN_JAR.
         */
        MAIN_JAR,
        /**
         * Contains test code, typically named "...-tests.jar".
         */
        TESTS_JAR,
        /**
         * Contains source code, typically named "...-sources.jar".
         */
        SOURCES_JAR,
        /**
         * Contains JavaDoc, typically named "...-javadoc.jar".
         */
        JAVADOC_JAR,
    }

    public static JarFileType jarFileType(File file) {
        String name = file.getName();
        if (!name.endsWith(".jar")) {
            throw new IllegalArgumentException("Not a jar file: " + file.getAbsolutePath());
        }
        return name.endsWith("-tests.jar") ? TESTS_JAR :
                name.endsWith("-sources.jar") ? SOURCES_JAR :
                        name.endsWith("-javadoc.jar") ? JAVADOC_JAR : MAIN_JAR;
    }

    public static File[] jarFilesInTargetOfMavenProject(
            @Nullable File mavenProjectDirectory, JarFileType... jarFileTypes) {
        if (mavenProjectDirectory == null) {
            return new File[0];
        }
        checkIsMavenProjectDirectory(mavenProjectDirectory);

        boolean includeMainJars = jarFileTypes.length == 0 ||
                ArrayUtil.contains(jarFileTypes, MAIN_JAR);
        boolean includeTests = ArrayUtil.contains(jarFileTypes, TESTS_JAR);
        boolean includeSources = ArrayUtil.contains(jarFileTypes, SOURCES_JAR);
        boolean includeJavaDoc = ArrayUtil.contains(jarFileTypes, JAVADOC_JAR);
        return filesInDirectoryAndDeeper(mavenProjectDirectory,
                f -> {
                    if (!f.getName().endsWith(".jar") ||
                            !isInTargetDirectory(f)) {
                        return false;
                    }
                    return switch (jarFileType(f)) {
                        case MAIN_JAR -> includeMainJars;
                        case TESTS_JAR -> includeTests;
                        case SOURCES_JAR -> includeSources;
                        case JAVADOC_JAR -> includeJavaDoc;
                    };
                    // to please the compiler ("missing return statement" error)
                });
    }

    public static File[] classpathOfMavenProject(File mavenProjectDirectory) {
        checkIsMavenProjectDirectory(mavenProjectDirectory);

        try {
            File tempFile = File.createTempFile("abego-jareento-maven-classpathOfMavenProject", ".txt");
            tempFile.deleteOnExit();
            String outputFileOption = "-Dmdep.outputFile=" + tempFile.getAbsolutePath();
            runMavenCommand(
                    new String[]{"dependency:build-classpath", outputFileOption}, mavenProjectDirectory);
            String result = FileUtil.textOf(tempFile);
            // the classpath may be returned in multiple lines. Join them,
            // using the System's path separator
            String joinedLines = result.replaceAll(
                    "\r?\n",
                    Matcher.quoteReplacement(System.getProperty("path.separator")));
            return FileUtil.parseFiles(joinedLines.trim());

        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("Error when calculating classpath of Maven project '%s'",
                            mavenProjectDirectory.getAbsolutePath()), e);
        }
    }

    private static void checkIsMavenProjectDirectory(File file) {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException(
                    String.format("Maven project directory expected. Got '%s'",
                            file.getAbsolutePath()));
        }

        File pomFile = new File(file, "pom.xml");
        if (!pomFile.isFile()) {
            throw new IllegalArgumentException(
                    String.format("Maven project expected. No 'pom.xml found in '%s'",
                            file.getAbsolutePath()));
        }
    }

    private static boolean isInTargetDirectory(File file) {
        @Nullable
        File f = file.getAbsoluteFile().getParentFile();
        while (f != null) {
            if (f.getName().equals("target")) {
                return true;
            }
            f = f.getParentFile();
        }
        return false;
    }
}
