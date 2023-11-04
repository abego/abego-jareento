package org.abego.jareento.shared.commons.maven;

import org.abego.commons.io.FileUtil;
import org.abego.commons.test.JUnit5Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenUtilTest {

    @Test
    void sourceDirectoryFromMavenProject(@TempDir File tempDir) {
        File pomXmlFile = new File(tempDir, "pom.xml");
        File sourceDir = MavenUtil.sourceDirectoryFromMavenProject(tempDir);

        assertEquals(
                new File(tempDir, "src/main/java").getAbsolutePath(),
                sourceDir.getAbsolutePath());

        FileUtil.writeText(pomXmlFile, """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                \t<modelVersion>4.0.0</modelVersion>
                \t<build>
                \t\t<sourceDirectory>src</sourceDirectory>
                \t\t<resources>
                \t\t\t<resource>
                \t\t\t\t<directory>resources</directory>
                \t\t\t</resource>
                \t\t</resources>
                \t</build>
                </project>
                """);

        sourceDir = MavenUtil.sourceDirectoryFromMavenProject(tempDir);

        assertEquals(
                new File(tempDir, "src").getAbsolutePath(),
                sourceDir.getAbsolutePath());
    }

    @Test
    void noPomXml(@TempDir File tempDir) {
        JUnit5Util.assertThrowsWithMessage(IllegalArgumentException.class,
                "Maven project expected. No 'pom.xml' found in '%s'"
                        .formatted(tempDir.getAbsolutePath()),
                () -> MavenUtil.classpathOfMavenProject(tempDir));
    }

    @Test
    void noDirectory() {
        File someFile = new File("foo.txt");
        JUnit5Util.assertThrowsWithMessage(IllegalArgumentException.class,
                "Maven project directory expected. Got '%s'"
                        .formatted(someFile.getAbsolutePath()),
                () -> MavenUtil.classpathOfMavenProject(someFile));
    }
    
    @Test
    void malformedPOM(@TempDir File tempDir) {
        File pomFile = new File(tempDir,"pom.xml");
        FileUtil.writeText(pomFile,"<foo></foo>");

        IllegalStateException e = JUnit5Util.assertThrowsWithMessage(IllegalStateException.class,
                "Error when calculating classpath of Maven project '%s'"
                        .formatted(tempDir.getAbsolutePath()),
                () -> MavenUtil.classpathOfMavenProject(tempDir));
        String causeMessage = e.getCause().getMessage();
        assertTrue(causeMessage.contains("[ERROR] Malformed POM"));
        assertTrue(causeMessage.contains("[ERROR] 'modelVersion' is missing. @ line 1, column 5"));
    }
}
