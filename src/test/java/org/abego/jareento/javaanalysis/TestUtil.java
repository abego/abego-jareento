package org.abego.jareento.javaanalysis;

import org.abego.commons.io.FileUtil;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestUtil {
    /**
     * Writes a file "Mini.java" containing an empty public class {@code Main}
     * into the given {@code directory}.
     */
    public static File writeMiniJavaFile(File directory) {
        File javaFile = new File(directory, "Mini.java");
        FileUtil.writeText(javaFile, "public class Mini {\n}\n");
        return javaFile;
    }

    public static File writePomFile(File directory) {
        File pomFile = new File(directory, "pom.xml");
        FileUtil.writeText(pomFile, "<project><modelVersion>4.0.0</modelVersion></project>");
        return pomFile;
    }

    public static void assertEqualsAndHashCodeOK(
            Object object, Object otherButEqualObject) {
        assertEquals(object, object);
        assertEquals(object, otherButEqualObject);
        assertNotEquals(object, null);

        assertEquals(object.hashCode(), otherButEqualObject.hashCode());
    }
}
