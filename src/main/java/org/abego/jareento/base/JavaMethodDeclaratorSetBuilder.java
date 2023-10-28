package org.abego.jareento.base;

import org.abego.jareento.shared.JavaMethodDeclarator;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.Syntax;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Logger.getLogger;
import static org.abego.jareento.base.JareentoSyntax.METHOD_DECLARATOR_SYNTAX;

/**
 * A Builder for {@link JavaMethodDeclaratorSet} objects.
 */
public final class JavaMethodDeclaratorSetBuilder {
    private static final Logger LOGGER = getLogger(JavaMethodDeclaratorSetBuilder.class.getName());

    private final Set<String> methods = new HashSet<>();
    private final Map<String, String> simpleMethodDeclaratorToMethodDeclarator = new HashMap<>();
    private final Set<String> classes = new HashSet<>();

    private JavaMethodDeclaratorSetBuilder() {
    }

    /**
     * Returns a new {@link JavaMethodDeclaratorSetBuilder}.
     */
    public static JavaMethodDeclaratorSetBuilder newJavaMethodSetBuilder() {
        return new JavaMethodDeclaratorSetBuilder();
    }

    /**
     * Returns the number of methods in the {@link JavaMethodDeclaratorSet}
     * currently under construction.
     */
    public int getSize() {
        return methods.size();
    }

    /**
     * Adds a method with the given {@code methodDeclarator} to the
     * {@link JavaMethodDeclaratorSet} currently under construction and returns
     * {@code this} {@link JavaMethodDeclaratorSetBuilder}, allowing this method to be
     * used in a call chain/cascade.
     */
    public JavaMethodDeclaratorSetBuilder addMethod(
            @Syntax(METHOD_DECLARATOR_SYNTAX) String methodDeclarator) {
        String classname = JavaMethodDeclarator.simpleClassnameOfMethodDeclarator(methodDeclarator);
        classes.add(classname);

        // also include the outer class when the method belongs
        // to an inner class.
        while (classname.contains("$")) {
            int i = classname.lastIndexOf("$");
            classname = classname.substring(0, i);
            classes.add(classname);
        }
        //TODO: shall we really replace the '$' by a '.' ??? Cannot tell a package from an outer class!
        String fixedMethodDeclarator = methodDeclarator.replaceAll("\\$", ".");
        methods.add(fixedMethodDeclarator);
        String simpleMethodDeclarator = JavaMethodDeclarator.simpleMethodDeclaratorOfMethodDeclarator(fixedMethodDeclarator);
        @Nullable
        String oldValue = simpleMethodDeclaratorToMethodDeclarator.put(
                simpleMethodDeclarator,
                fixedMethodDeclarator);
        if (oldValue != null) {
            LOGGER.warning(String.format("Method %s already defined with different return type: %s",
                    fixedMethodDeclarator, oldValue));
        }
        return this;
    }

    /**
     * Adds the methods as defined by the {@code methodDeclaratorTexts} to the 
     * {@link JavaMethodDeclaratorSet} currently under construction and 
     * returns {@code this} {@link JavaMethodDeclaratorSetBuilder},
     * allowing this method to be used in a call chain/cascade.
     */
    public JavaMethodDeclaratorSetBuilder addAllMethods(
            @Syntax(METHOD_DECLARATOR_SYNTAX) Iterable<String> methodDeclaratorTexts) {
        for (String m : methodDeclaratorTexts) {
            addMethod(m);
        }
        return this;
    }

    /**
     * Returns a freshly build {@link JavaMethodDeclaratorSet} based on the
     * {@link #addMethod(String)} and {@link #addAllMethods(Iterable)} calls
     * up to this time.
     * <p>
     * Calling this method will not end the construction nor clear the currently
     * collected set of methods in the builder. You may call this method
     * multiple times, possibly after additional calls to {@link #addMethod(String)}
     * etc. It will always return new JavaMethodSet instances, with all methods
     * collected so far.
     */
    public JavaMethodDeclaratorSet build() {
        return new JavaMethodDeclaratorSet() {
            @Override
            public int getSize() {
                return methods.size();
            }

            @Override
            public boolean containsMethodOfTypeWithSignature(String type, String signature) {
                return methodDeclaratorTextOfMethodOfTypeWithSignatureOrNull(type, signature) != null;
            }

            @Override
            public @Nullable String methodDeclaratorTextOfMethodOfTypeWithSignatureOrNull(String type, String signature) {
                return simpleMethodDeclaratorToMethodDeclarator.get(JavaMethodDeclarator.simpleMethodDeclaratorText(type, signature));
            }

            @Override
            public boolean containsMethodOfClass(String classname) {
                return classes.contains(classname);
            }

            @Override
            public Stream<String> stream() {
                return methods.stream();
            }
        };
    }
}
