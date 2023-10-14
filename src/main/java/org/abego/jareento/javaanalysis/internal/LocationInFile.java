package org.abego.jareento.javaanalysis.internal;

import com.github.javaparser.ast.Node;
import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;
import org.abego.stringpool.MutableStringPool;

import java.io.File;

import static org.abego.stringpool.StringPools.newMutableStringPool;

/**
 * Identifies a location in a file, defined by the {@link File} and the
 * line number.
 * <p>
 * No "real" instances are created of this class but the location is stored in a
 * {@code long} value, called the "id" of the location. Use
 * {@link #getId(File, long)} to get the id for a given File and line number.
 * To access the File and line number pass that id to the methods
 * {@link #getFile(long)} or {@link #getLineNumber(long)}.
 * <p>
 * The class keeps the pathes of all files used with this class in memory.
 * They are never freed. This may lead to memory issues in certain scenarios.
 */
//TODO: open this class? Already used outside.
public final class LocationInFile {
    LocationInFile() {
        throw new MustNotInstantiateException();
    }

    private static final MutableStringPool allFilePaths = newMutableStringPool();

    /**
     * Returns the id of the location in file given by {@code file} and
     * {@code lineNumber}.
     * <p>
     * See JavaDoc of {@link LocationInFile} for details.
     */
    public static long getId(File file, long lineNumber) {
        String path = file.getAbsolutePath();
        int fileId = allFilePaths.add(path);
        return (lineNumber << 32) | fileId;
    }

    /**
     * Returns the id of the location in file for the beginning of the
     * {@code node}
     */
    public static long getId(Node node) {
        return getId(
                JavaParserUtil.fileOf(node),
                JavaParserUtil.lineNumberOfBeginOf(node));
    }

    public static File getFile(long id) {
        long fileId = id & 0xffffffffL;
        return new File(allFilePaths.getString((int) fileId));
    }

    public static int getLineNumber(long id) {
        return (int) (id >> 32);
    }

}
