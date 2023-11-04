package org.abego.jareento.shared.commons.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.abego.commons.io.FileUtil;
import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.abego.commons.test.JUnit5Util;
import org.abego.jareento.base.JareentoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;

import static org.abego.jareento.shared.commons.javaparser.JavaParserUtil.fieldNamed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class JavaParserUtilTest {

    @Test
    void constructor() {
        assertThrows(MustNotInstantiateException.class, JavaParserUtil::new);
    }

    @Test
    void fieldNamedAndResolveType() throws FileNotFoundException {
        String srcPath = "src/test/data/javaprojects/barbazfoo";
        String classFooPath = srcPath + "/com/example/barbazfoo/Foo.java";

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(new JavaParserTypeSolver(new File(srcPath)));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
        StaticJavaParser.getParserConfiguration()
                .setSymbolResolver(symbolSolver);

        CompilationUnit cu = StaticJavaParser.parse(new File(classFooPath));

        VariableDeclarator barInFoo = fieldNamed(cu, "Foo.bar");
        VariableDeclarator barInFooZum = fieldNamed(cu, "Foo.Zum.bar");

        assertEquals("com.example.barbazfoo.Bar",
                JavaParserUtil.resolveType(barInFoo).getQualifiedName());
        assertEquals("com.example.barbazfoo.Foo.Zum.Bar",
                JavaParserUtil.resolveType(barInFooZum).getQualifiedName());
    }

    @Test
    void fieldNamedAndResolveStandardTypes() throws FileNotFoundException {
        String srcPath = "src/test/data/javaprojects/barbazfoo";
        String testeeClassPath = srcPath + "/com/example/barbazfoo/Bar.java";

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(new ReflectionTypeSolver());
        combinedSolver.add(new JavaParserTypeSolver(new File(srcPath)));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
        StaticJavaParser.getParserConfiguration()
                .setSymbolResolver(symbolSolver);

        CompilationUnit cu = StaticJavaParser.parse(new File(testeeClassPath));

        VariableDeclarator objectFieldInBar = fieldNamed(cu, "Bar.objectField");
        VariableDeclarator stringFieldInBar = fieldNamed(cu, "Bar.stringField");
        VariableDeclarator bazFieldInBar = fieldNamed(cu, "Bar.bazField");

        assertEquals("java.lang.Object",
                JavaParserUtil.resolveType(objectFieldInBar)
                        .getQualifiedName());
        assertEquals("java.lang.String",
                JavaParserUtil.resolveType(stringFieldInBar)
                        .getQualifiedName());
        assertEquals("com.example.barbazfoo.Baz",
                JavaParserUtil.resolveType(bazFieldInBar).getQualifiedName());
    }

    @Test
    void fieldNamedMissingPathInFieldName(@TempDir File tempDir) throws FileNotFoundException {
        File javaFile = new File(tempDir, "Main.java");
        FileUtil.writeText(javaFile, "public class Main {}");
        CompilationUnit cu = StaticJavaParser.parse(javaFile);

        JUnit5Util.assertThrowsWithMessage(
                IllegalArgumentException.class,
                "Field path expected, including class part. Got 'foo'.",
                () -> JavaParserUtil.fieldNamed(cu, "foo"));
    }

    @Test
    void forEachJavaFileDoInvalidSourceRoot() {
        File fooTxtFile = new File("foo.txt");

        InvalidSourceRootException e = JUnit5Util.assertThrowsWithMessage(
                InvalidSourceRootException.class,
                "Source roots must only contain directories (to Java source code) or jar files. Got: '%s'"
                        .formatted(fooTxtFile.getAbsolutePath()),

                () -> JavaParserUtil.forEachJavaFileDo(
                        new File[]{fooTxtFile},
                        new File[0],
                        cu -> {}));
        assertEquals(fooTxtFile,e.getFile());
    }

    @Test
    void forEachJavaFileDoParseError(@TempDir File tempDir) {
        File javaFile = new File(tempDir, "Main.java");
        FileUtil.writeText(javaFile, "public class Main {");

        JUnit5Util.assertThrowsWithMessage(
                JareentoException.class,
                "Error when parsing '%s'"
                        .formatted(javaFile.getAbsolutePath()),
                () -> JavaParserUtil.forEachJavaFileDo(
                        new File[]{tempDir},
                        new File[0],
                        f -> true,
                        c -> {},
                        s -> {}));
    }

    @Test
    void saveToFileReadOnlyFile(@TempDir File tempDir) throws FileNotFoundException {
        File javaFile = new File(tempDir, "Main.java");
        FileUtil.writeText(javaFile, "public class Main {}");
        CompilationUnit cu = StaticJavaParser.parse(javaFile);

        // make file readonly, so saving will fail. 
        if (!javaFile.setWritable(false)) {
            fail("Failed to set file readonly");
        }
        JUnit5Util.assertThrowsWithMessage(
                java.io.UncheckedIOException.class,
                "java.io.FileNotFoundException: %s (Permission denied)"
                        .formatted(javaFile.getAbsolutePath()),
                () -> JavaParserUtil.saveToFile(cu));
    }

    @Test
    void lineNumberOfBeginOf(@TempDir File tempDir) throws FileNotFoundException {
        File javaFile = new File(tempDir, "Main.java");
        FileUtil.writeText(javaFile, "public class Main {}");
        CompilationUnit cu = StaticJavaParser.parse(javaFile);

        assertEquals(1, JavaParserUtil.lineNumberOfBeginOf(cu));
    }

    @Test
    void positionOfBeginOf(@TempDir File tempDir) throws FileNotFoundException {
        File javaFile = new File(tempDir, "Main.java");
        FileUtil.writeText(javaFile, "public class Main {}");
        CompilationUnit cu = StaticJavaParser.parse(javaFile);

        assertEquals(1, JavaParserUtil.positionOfBeginOf(cu).line);
    }

    @Test
    void compilationUnitOf(@TempDir File tempDir) throws FileNotFoundException {
        File javaFile = new File(tempDir, "Main.java");
        FileUtil.writeText(javaFile, "public class Main {}");
        CompilationUnit cu = StaticJavaParser.parse(javaFile);
        TypeDeclaration<?> type1 = cu.getType(0);

        assertEquals(cu, JavaParserUtil.compilationUnitOf(cu));
        assertEquals(cu, JavaParserUtil.compilationUnitOf(type1));
    }
}
