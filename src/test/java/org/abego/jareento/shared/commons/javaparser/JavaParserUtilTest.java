package org.abego.jareento.shared.commons.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.abego.jareento.shared.commons.javaparser.JavaParserUtil.fieldNamed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

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
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

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


}
