package org.abego.jareento.shared.commons.javaparser;

import java.io.File;

import static org.abego.commons.io.FileUtil.canonicalPath;

class InvalidSourceRootException extends RuntimeException {
    private final File file;

    public InvalidSourceRootException(File file) {
        super(calcMessageText(file), null);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    private static String calcMessageText(File file) {
        return String.format("Source roots must only contain directories (to Java source code) or jar files. Got: '%s'",
                canonicalPath(file));
    }
}
