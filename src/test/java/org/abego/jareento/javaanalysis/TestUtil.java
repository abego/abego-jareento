package org.abego.jareento.javaanalysis;

import org.abego.commons.io.FileUtil;

import java.io.File;

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
}
