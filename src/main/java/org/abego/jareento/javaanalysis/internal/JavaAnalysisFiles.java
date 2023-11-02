package org.abego.jareento.javaanalysis.internal;

import com.github.javaparser.ast.CompilationUnit;
import org.abego.jareento.shared.commons.javaparser.JavaParserUtil;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface JavaAnalysisFiles {

    /**
     * Returns the root directories of the source code to be analysed.
     * <p>
     * These are directories like 'src/main/java' in a Maven project.
     */
    File[] getSourceRoots();
    
    /**
     * Returns the files to be used to resolve type references in the source
     * code to be analysed.
     * <p>
     * Each file in this array is either
     * <ul>
     * <li>a source directory of Java code (like 'src/main/java' in a
     * Maven project), or</li>
     * <li>a `*.jar` file.</li>
     * </ul>
     */
    File[] getDependencies();

    default void withCompilationUnitsDo(
            Consumer<CompilationUnit> operation,
            Predicate<File> fileFilter,
            Consumer<String> progress) {
        JavaParserUtil.forEachJavaFileDo(
                getSourceRoots(),
                getDependencies(),
                fileFilter, operation, progress);
    }

}
