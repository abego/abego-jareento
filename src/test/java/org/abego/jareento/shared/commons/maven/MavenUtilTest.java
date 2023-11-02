package org.abego.jareento.shared.commons.maven;

import org.abego.commons.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
