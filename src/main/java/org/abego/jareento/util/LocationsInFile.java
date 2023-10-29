package org.abego.jareento.util;

import com.github.javaparser.ast.Node;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;
import org.abego.stringpool.MutableStringPool;

import java.io.File;

import static org.abego.stringpool.StringPools.newMutableStringPool;

/**
 * Associates a location in a file, defined by the {@link File} and the
 * line number, with a numeric ID and provides operations to retrieve the
 * {@code File} and line number based on the ID.
 * <p>
 * Use {@link #getId(File, long)} to get the id for a given File and line number.
 * To access the File and line number pass that id to the methods
 * {@link #getFile(long)} or {@link #getLineNumber(long)}.
 * <p>
 * You may create {@link LocationsInFile} instances using 
 * {@link #newLocationsInFile()}, or you may use the default {@code LocationInFile} 
 * instance, accessible through {@link #getInstance()}. 
 * <p>
 * Use this class when many locations need to be managed and memory may
 * become of a concern if this information would be managed in "plain"
 * records containing the File and long values. In this class the related
 * information is represented in a compact form, reducing the memory footprint
 * compared to a naive implementation.
 * <p>
 * Regarding the default {@code LocationInFile} instance: as the default
 * instance is never released all memory associated with it (e.g. file pathes of
 * file locations) is never released. This may lead to memory issues in certain 
 * scenarios.
 */
public final class LocationsInFile {
    private static final LocationsInFile INSTANCE = newLocationsInFile();

    private final MutableStringPool allFilePaths = newMutableStringPool();

    private LocationsInFile() {
    }

    public static LocationsInFile getInstance() {
        return INSTANCE;
    }

    public static LocationsInFile newLocationsInFile() {
        return new LocationsInFile();
    }
    
    /**
     * Returns the id of the location in file given by {@code file} and
     * {@code lineNumber}.
     * <p>
     * See JavaDoc of {@link LocationsInFile} for details.
     */
    public long getId(File file, long lineNumber) {
        if (lineNumber < 0 || lineNumber > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid lineNumber. Got: %d".formatted(lineNumber));
        }
        String path = file.getAbsolutePath();
        int fileId = allFilePaths.add(path);
        return (lineNumber << 32) | fileId;
    }

    /**
     * Returns the id of the location in file for the beginning of the
     * JavaParser {@code node}
     */
    public long getId(Node node) {
        return getId(
                JavaParserUtil.fileOf(node),
                JavaParserUtil.lineNumberOfBeginOf(node));
    }

    /**
     * Returns the {@link File} of the location in file associated with the given
     * {@code id}
     */
    public File getFile(long id) {
        long fileId = id & 0xffffffffL;
        return new File(allFilePaths.getString((int) fileId));
    }

    /**
     * Returns the line number of the location in file associated with the given
     * {@code id}
     */
    public int getLineNumber(long id) {
        return (int) (id >> 32);
    }

}
