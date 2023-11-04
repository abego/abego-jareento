package org.abego.jareento.shared.commons.javaparser;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Position;
import com.github.javaparser.Processor;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.Printer;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.Navigator;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.abego.commons.io.FileUtil;
import org.abego.commons.lang.ArrayUtil;
import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.abego.commons.progress.ProgressWithRange;
import org.abego.commons.progress.Progresses;
import org.abego.commons.var.Var;
import org.abego.jareento.base.JareentoException;
import org.abego.jareento.shared.SyntaxUtil;
import org.abego.jareento.util.JavaLangUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.abego.commons.io.FileUtil.withFilesInDirectoryAndDeeperDo;
import static org.abego.commons.var.VarUtil.newVar;
import static org.abego.jareento.shared.commons.progress.ProgressWithRangeListenerWithStringConsumer.newProgressListenerWithStringConsumer;

public final class JavaParserUtil {
    private static final Supplier<IllegalStateException> INTERNAL_ERROR =
            () -> new IllegalStateException("Internal Error");

    JavaParserUtil() {
        throw new MustNotInstantiateException();
    }

    public static VariableDeclarator fieldNamed(
            CompilationUnit cu, String fieldNamePath) {
        String[] parts = SyntaxUtil.qualifierAndSimpleName(fieldNamePath);
        String classOrInterfaceName = parts[0];
        String fieldName = parts[1];

        if (classOrInterfaceName.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "Field path expected, including class part. Got '%s'.", fieldNamePath));
        }

        return fieldNamedInClass(cu, fieldName, classOrInterfaceName);
    }

    public static VariableDeclarator fieldNamed(
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration, String fieldName) {
        return Navigator.demandField(classOrInterfaceDeclaration, fieldName);
    }

    public static ResolvedReferenceType resolveType(VariableDeclarator variableDeclarator) {
        return resolveType(variableDeclarator.getType());
    }

    public static ResolvedReferenceType resolveType(Type type) {
        return type.resolve().asReferenceType();
    }

    public static void saveToFile(CompilationUnit cu) {
        String newText = LexicalPreservingPrinter.print(cu);
        cu.getStorage().ifPresentOrElse(s ->
                        //TODO: custom encoding, also when reading files.
                        FileUtil.writeText(s.getPath().toFile(), newText),
                () -> {throw new JareentoException("No storage defined");});
    }


    private static VariableDeclarator fieldNamedInClass(CompilationUnit cu, String fieldName, String className) {
        return fieldNamed(Navigator.demandClass(cu, className), fieldName);
    }

    public static void forEachJavaFileDo(
            File[] sourceRoots,
            File[] dependencies,
            Consumer<CompilationUnit> compilationUnitHandler) {
        forEachJavaFileDo(sourceRoots, dependencies, compilationUnitHandler, e -> {});
    }

    public static void forEachJavaFileDo(
            File[] sourceRoots,
            File[] dependencies,
            Consumer<CompilationUnit> compilationUnitHandler,
            Consumer<String> progress) {
        forEachJavaFileDo(sourceRoots, dependencies, f -> true, compilationUnitHandler, progress);
    }

    public static void forEachJavaFileDo(
            File[] sourceRoots,
            File[] dependencies,
            Predicate<File> javaFileSelector,
            Consumer<CompilationUnit> compilationUnitHandler,
            Consumer<String> progress) {

        SymbolResolver resolver = getSymbolResolverForSourceRootsAndDependencies(
                ArrayUtil.concatenate(sourceRoots, dependencies));
        StaticJavaParser.getParserConfiguration().setSymbolResolver(resolver);
        usePrinterWithoutComments(StaticJavaParser.getParserConfiguration());
        // for performance and memory reasons, no comments support
        StaticJavaParser.getParserConfiguration().setAttributeComments(false);
        StaticJavaParser.getParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

        Predicate<File> filter = (file) -> file.getName()
                .endsWith(".java") && javaFileSelector.test(file);

        int fileCount = countFilesInDirectoriesAndDeeper(
                sourceRoots, filter);

        Var<Integer> index = newVar(0);
        ProgressWithRange progressWithRange = Progresses.createProgressWithRange(
                "Processing Java files ...", fileCount,
                newProgressListenerWithStringConsumer(progress));
        try {
            for (File directory : sourceRoots) {
                withFilesInDirectoryAndDeeperDo(directory, filter, file -> {
                    progressWithRange.update(index.get(), file.getAbsolutePath());
                    index.set(index.get() + 1);
                    try {
                        CompilationUnit compilationUnit = StaticJavaParser.parse(file);
                        compilationUnitHandler.accept(compilationUnit);
                    } catch (Exception e) {
                        throw new JareentoException(
                                String.format("Error when parsing '%s'", file.getAbsolutePath()),
                                e);
                    }
                });
            }
            progressWithRange.update(fileCount, "Done.");
        } finally {
            progressWithRange.close();
        }
    }

    private static int countFilesInDirectoriesAndDeeper(
            File[] directories, Predicate<File> selector) {
        Var<Integer> count = newVar(0);
        for (File directory : directories) {
            withFilesInDirectoryAndDeeperDo(
                    directory, selector, f -> count.set(count.get() + 1));
        }
        return count.get();
    }

    private static SymbolResolver getSymbolResolverForSourceRootsAndDependencies(
            File[] sourceRootsAndDependencies) {
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(new ReflectionTypeSolver());
        for (File file : sourceRootsAndDependencies) {
            combinedSolver.add(getTypeSolverForSourceRootOrJar(file));
        }
        return new JavaSymbolSolver(combinedSolver);
    }

    private static TypeSolver getTypeSolverForSourceRootOrJar(File file) {
        if (file.isDirectory()) {
            return new JavaParserTypeSolver(file, StaticJavaParser.getParserConfiguration());
        }

        // everything else should be a jar file
        if (!file.getName().endsWith(".jar")) {
            throw new InvalidSourceRootException(file);
        }
        try {
            return new JarTypeSolver(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Sets the printer of compilation units processed by the given
     * {@code parserConfiguration} to a {@link DefaultPrettyPrinter}
     * that does not print comments.
     * <p>
     * When working with the {@link StaticJavaParser} you will typically use
     * code like this:
     * <pre>
     *         usePrinterWithoutComments(StaticJavaParser.getConfiguration());
     * </pre>
     * to use the "no comments" printer. Also make sure to pass around the
     * modified {@link ParserConfiguration}, e.g. when creating
     * {@link JavaParserTypeSolver} instances:
     * <pre>
     *      new JavaParserTypeSolver(file, StaticJavaParser.getConfiguration());
     * </pre>
     * <b>Background</b>
     * <p>
     * By default, a compilation units uses the standard DefaultPrettyPrinter to
     * "print" a node. In the default mode this would also print comments.
     * Printing comments, or even checking, if a comment must be printed, is
     * quite time and memory consuming (For details have a look at
     * {@code printOrphanCommentsBeforeThisChildNode(com.github.javaparser.ast.Node)}
     * in {@link com.github.javaparser.printer.DefaultPrettyPrinterVisitor}
     * and check what code it executes, or profile its execution.)
     * On the other hand "printing" is also used to get the "name" of a node,
     * e.g. when resolving types. When resolving types is a frequent operation
     * the "comment" support significantly slows down the execution.
     * In that case make sure to use the printer without comments.
     */
    private static void usePrinterWithoutComments(ParserConfiguration parserConfiguration) {

        DefaultPrinterConfiguration noCommentsConfiguration =
                new DefaultPrinterConfiguration();
        noCommentsConfiguration.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS));
        Printer myPrettyPrinter = new DefaultPrettyPrinter(noCommentsConfiguration);
        parserConfiguration.getProcessors().add(() -> new Processor() {
            @Override
            public void postProcess(ParseResult<? extends Node> result, ParserConfiguration configuration) {
                Optional<? extends Node> value = result.getResult();
                if (value.isPresent() && value.get() instanceof CompilationUnit) {
                    ((CompilationUnit) value.get()).printer(myPrettyPrinter);
                }
            }
        });
    }


    public static String getStorageFileName(CompilationUnit cu) {
        return cu.getStorage().orElseThrow(NoSuchElementException::new)
                .getFileName();
    }

    public static List<String> asQualifiedTypeNames(NodeList<ClassOrInterfaceType> types) {
        return types.stream()
                .map(t -> t.resolve().asReferenceType().getQualifiedName())
                .collect(Collectors.toList());
    }

    public static String[] parameterTypes(
            ResolvedMethodLikeDeclaration resolvedMethodLikeDeclaration) {
        int n = resolvedMethodLikeDeclaration.getNumberOfParams();
        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            result[i] = resolvedMethodLikeDeclaration.getParam(i)
                    .describeType();
        }
        return result;
    }

    public static String methodSignatureWithRawTypes(ResolvedMethodDeclaration resolvedMethodDeclaration) {
        // based on com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration.getSignature
        StringBuilder sb = new StringBuilder();
        sb.append(resolvedMethodDeclaration.getName());
        sb.append("(");
        for (int i = 0; i < resolvedMethodDeclaration.getNumberOfParams(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(JavaLangUtil.rawName(
                    resolvedMethodDeclaration.getParam(i).describeType()));
        }
        sb.append(")");
        return sb.toString();
    }

    public static CompilationUnit compilationUnitOf(Node node) {
        //noinspection unchecked
        return node instanceof CompilationUnit
                ? (CompilationUnit) node
                : node.findAncestor(CompilationUnit.class)
                .orElseThrow(INTERNAL_ERROR);
    }

    public static Position positionOfBeginOf(Node node) {
        return node.getRange().orElseThrow(INTERNAL_ERROR).begin;
    }

    public static int lineNumberOfBeginOf(Node node) {
        return positionOfBeginOf(node).line;
    }

    public static File fileOf(Node node) {
        CompilationUnit cu = compilationUnitOf(node);
        Path path = cu.getStorage().orElseThrow(INTERNAL_ERROR).getPath();
        return path.toFile();
    }

}
