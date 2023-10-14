package org.abego.jareento.javarefactoring.internal;

import org.abego.commons.io.FileUtil;
import org.abego.commons.util.PropertiesGroup;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.Properties;

import static org.abego.commons.util.PropertiesGroup.newPropertiesGroup;

public class TestData {
    //TODO: fix PropertiesGroup name
    private static final PropertiesGroup propertiesGroup =
            newPropertiesGroup("abego.jareento");

    public static Properties getProperties() {
        return propertiesGroup.getProperties();
    }

    @Nullable
    public static String getPropertyOrNull(String key) {
        return getProperties().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    public static File getBigSampleJavapFile() {
        String filePath = getProperty(
                "abego-jareento.big-java-sample-javap-file",
                new File("javap-big-sample.txt").getAbsolutePath());
        return new File(filePath);
    }

    public static File getBigSampleBaseDirectory() {
        String filePath = getProperty(
                "abego-jareento.big-java-sample-baseDirectory",
                new File("").getAbsolutePath());

        return new File(filePath);
    }

    public static File getBigSampleJavaAnalyzeProjectFile() {
        String filePath = getProperty(
                "abego-jareento.big-java-sample-jas-file",
                new File("untracked-data/bigsample.jas").getAbsolutePath());

        return new File(filePath);
    }

    public static File getBigSampleJDepsDOTFile() {
        String filePath = getProperty(
                "abego-jareento.big-java-sample-jdeps-dot-file",
                new File("jdeps.dot").getAbsolutePath());

        return new File(filePath);
    }

    public static File getBigSampleJDepsJavaSourceCodeJavaAnalysisProjectTestOutputFile() {
        return new File("target/test-output/bigsample-jdeps-javasourcecode.jas");
    }

    //TODO move to commons (FileUtil) (some similar method already exists)
    //TODO introduce a "test data" class that provides common test data
    private static void copyResourcesToDirectory(
            File source,
            Class<?> rootClass,
            String relativeResourceDirectoryPath,
            String... fileNames) {
        for (String name : fileNames) {
            //noinspection StringConcatenation
            FileUtil.copyResourceToFile(
                    rootClass,
                    relativeResourceDirectoryPath + name,
                    new File(source, name));
        }
    }

    public static void copySampleProjectTo(File tempDir) {
        copyResourcesToDirectory(tempDir, TestData.class, "sampleproject/",
                "com/example/Base.java",
                "com/example/InterfaceA.java",
                "com/example/Main.java",
                "com/example/Sub1.java",
                "com/example/Sub2.java");
    }


}
