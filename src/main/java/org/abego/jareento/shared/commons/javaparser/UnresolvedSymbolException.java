package org.abego.jareento.shared.commons.javaparser;

import java.io.File;

import static org.abego.commons.io.FileUtil.canonicalPath;

class UnresolvedSymbolException extends RuntimeException {
    private final String symbolName;
    private final File file;

    public UnresolvedSymbolException(String symbolName, File file, Throwable cause) {
        super(calcMessageText(symbolName, file), cause);
        this.symbolName = symbolName;
        this.file = file;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public File getFile() {
        return file;
    }

    private static String calcMessageText(String symbolName, File file) {
        return String.format("Unresolved symbol '%s' in file '%s'",
                symbolName, canonicalPath(file));
    }
}
