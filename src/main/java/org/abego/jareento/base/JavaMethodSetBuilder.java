package org.abego.jareento.base;

import org.abego.jareento.shared.FullMethodDeclarator;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.Syntax;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Logger.getLogger;
import static org.abego.jareento.base.JareentoSyntax.FULL_METHOD_DECLARATOR_SYNTAX;
import static org.abego.jareento.shared.FullMethodDeclarator.methodDeclaratorText;

/**
 * A Builder for {@link JavaMethodSet} objects.
 */
public final class JavaMethodSetBuilder {
    private static final Logger LOGGER = getLogger(JavaMethodSetBuilder.class.getName());

    private final Set<String> methods = new HashSet<>();
    private final Map<String, String> methodDeclaratorToFullMethodDeclarator = new HashMap<>();
    private final Set<String> classes = new HashSet<>();

    private JavaMethodSetBuilder() {
    }

    /**
     * Returns a new {@link JavaMethodSetBuilder}.
     */
    public static JavaMethodSetBuilder newJavaMethodSetBuilder() {
        return new JavaMethodSetBuilder();
    }

    /**
     * Returns the number of methods in the {@link JavaMethodSet}
     * currently under construction.
     */
    public int methodCount() {
        return methods.size();
    }

    /**
     * Adds a method with the given {@code fullMethodDeclarator} to the
     * {@link JavaMethodSet} currently under construction and returns
     * {@code this} {@link JavaMethodSetBuilder}, allowing this method to be
     * used in a call chain/cascade.
     */
    public JavaMethodSetBuilder addMethod(
            @Syntax(FULL_METHOD_DECLARATOR_SYNTAX) String fullMethodDeclarator) {
        String classname = FullMethodDeclarator.simpleClassname(fullMethodDeclarator);
        classes.add(classname);

        // also include the outer class when the method belongs
        // to an inner class.
        while (classname.contains("$")) {
            int i = classname.lastIndexOf("$");
            classname = classname.substring(0, i);
            classes.add(classname);
        }
        //TODO: shall we really replace the '$' by a '.' ??? Cannot tell a package from an outer class!
        String fixedFullMethodDeclarator = fullMethodDeclarator.replaceAll("\\$", ".");
        methods.add(fixedFullMethodDeclarator);
        String methodDeclarator = FullMethodDeclarator.methodDeclarator(fixedFullMethodDeclarator);
        @Nullable
        String oldValue = methodDeclaratorToFullMethodDeclarator.put(
                methodDeclarator,
                fixedFullMethodDeclarator);
        if (oldValue != null) {
            LOGGER.warning(String.format("Method %s already defined with different return type: %s",
                    fixedFullMethodDeclarator, oldValue));
        }
        return this;
    }

    /**
     * Adds the methods with the FullMethodDeclarator contained in the
     * {@code fullMethodDeclarator} to the  {@link JavaMethodSet} currently
     * under construction and returns {@code this} {@link JavaMethodSetBuilder},
     * allowing this method to be used in a call chain/cascade.
     */
    public JavaMethodSetBuilder addAllMethods(@Syntax(FULL_METHOD_DECLARATOR_SYNTAX) Iterable<String> fullMethodDeclarators) {
        for (String m : fullMethodDeclarators) {
            addMethod(m);
        }
        return this;
    }

    /**
     * Returns a freshly build {@link JavaMethodSet} based on the
     * {@link #addMethod(String)} and {@link #addAllMethods(Iterable)} calls
     * up to this time.
     * <p>
     * Calling this method will not end the construction nor clear the currently
     * collected set of methods in the builder. You may call this method
     * multiple times, possibly after additional calls to {@link #addMethod(String)}
     * etc. It will always return new JavaMethodSet instances, with all methods
     * collected so far.
     */
    public JavaMethodSet build() {
        return new JavaMethodSet() {
            @Override
            public int getSize() {
                return methods.size();
            }

            @Override
            public boolean containsMethodOfTypeWithSignature(String type, String signature) {
                return fullMethodDeclaratorOfMethodOfTypeWithSignatureOrNull(type, signature) != null;
            }

            @Override
            public @Nullable String fullMethodDeclaratorOfMethodOfTypeWithSignatureOrNull(String type, String signature) {
                return methodDeclaratorToFullMethodDeclarator.get(methodDeclaratorText(type, signature));
            }

            @Override
            public boolean containsMethodOfClass(String classname) {
                return classes.contains(classname);
            }

            @Override
            public Stream<String> declaratorStream() {
                return methods.stream();
            }
        };
    }
}
