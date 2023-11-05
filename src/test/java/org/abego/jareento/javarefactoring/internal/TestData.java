package org.abego.jareento.javarefactoring.internal;

import org.abego.commons.io.FileUtil;
import org.abego.commons.util.PropertiesGroup;

import java.io.File;
import java.util.Properties;

import static org.abego.commons.util.PropertiesGroup.newPropertiesGroup;

public class TestData {
    private static final PropertiesGroup propertiesGroup =
            newPropertiesGroup("abego.jareento");

    public static Properties getProperties() {
        return propertiesGroup.getProperties();
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

    public static void copySampleProjectTo(File directory) {
        String[] javaFiles = {
                "com/example/Base.java",
                "com/example/InterfaceA.java",
                "com/example/Main.java",
                "com/example/Sub1.java",
                "com/example/Sub2.java"};
        for (String name : javaFiles) {
            //noinspection StringConcatenation
            FileUtil.copyResourceToFile(
                    TestData.class,
                    "sampleproject/" + name,
                    new File(directory, name));
        }
    }
}
