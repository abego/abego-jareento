package org.abego.jareento.javaanalysis;

import org.abego.jareento.shared.JavaMethodDeclaratorUtil;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.Syntax;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Logger.getLogger;
import static org.abego.jareento.base.JareentoSyntax.METHOD_DECLARATOR_SYNTAX;

/**
 * A builder for {@link JavaMethodDeclarators} instances.
 * <p>
 * Every builder can only create one {@code JavaMethodDeclarators} instance.
 */
public final class JavaMethodDeclaratorsBuilder {
    private static final Logger LOGGER = getLogger(JavaMethodDeclaratorsBuilder.class.getName());

    private final Set<String> methodDeclarators = new HashSet<>();
    private final Map<String, String> simpleMethodDeclaratorToMethodDeclarator = new HashMap<>();
    private final Set<String> classes = new HashSet<>();
    private boolean buildFinished;

    private JavaMethodDeclaratorsBuilder() {
    }

    /**
     * Returns a new {@link JavaMethodDeclaratorsBuilder}.
     */
    public static JavaMethodDeclaratorsBuilder newJavaMethodDeclaratorsBuilder() {
        return new JavaMethodDeclaratorsBuilder();
    }

    /**
     * Returns the number of methods in the {@link JavaMethodDeclarators}
     * currently under construction.
     */
    public int getSize() {
        return methodDeclarators.size();
    }

    /**
     * Adds a method with the given {@code methodDeclarator} to the
     * {@link JavaMethodDeclarators} currently under construction and returns
     * {@code this} {@link JavaMethodDeclaratorsBuilder}, allowing this method to be
     * used in a call chain/cascade.
     */
    public JavaMethodDeclaratorsBuilder addMethod(
            @Syntax(METHOD_DECLARATOR_SYNTAX) String methodDeclarator) {
        checkNotFinished();

        String classname = JavaMethodDeclaratorUtil.simpleClassnameOfMethodDeclarator(methodDeclarator);
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
        methodDeclarators.add(fixedMethodDeclarator);
        String simpleMethodDeclarator = JavaMethodDeclaratorUtil.simpleMethodDeclaratorOfMethodDeclarator(fixedMethodDeclarator);
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
     * {@link JavaMethodDeclarators} currently under construction and
     * returns {@code this} {@link JavaMethodDeclaratorsBuilder},
     * allowing this method to be used in a call chain/cascade.
     */
    public JavaMethodDeclaratorsBuilder addAllMethods(
            @Syntax(METHOD_DECLARATOR_SYNTAX) Iterable<String> methodDeclaratorTexts) {
        checkNotFinished();

        for (String m : methodDeclaratorTexts) {
            addMethod(m);
        }
        return this;
    }

    /**
     * Returns a freshly build {@link JavaMethodDeclarators} based on the
     * {@link #addMethod(String)} and {@link #addAllMethods(Iterable)} calls
     * up to this time.
     * <p>
     * Calling this method will not end the construction nor clear the currently
     * collected set of methods in the builder. You may call this method
     * multiple times, possibly after additional calls to {@link #addMethod(String)}
     * etc. It will always return new JavaMethodSet instances, with all methods
     * collected so far.
     */
    public JavaMethodDeclarators build() {
        buildFinished = true;
        return new JavaMethodDeclaratorsImpl();
    }

    private void checkNotFinished() {
        if (buildFinished) {
            throw new IllegalStateException("Build already finished.");
        }
    }

    private class JavaMethodDeclaratorsImpl implements JavaMethodDeclarators {
        private @Nullable List<JavaMethodDeclarator> methods;
        
        @Override
        public int getSize() {
            return methodDeclarators.size();
        }

        @Override
        public Stream<JavaMethodDeclarator> stream() {
            return textStream().map(JavaMethodDeclaratorUtil::newJavaMethodDeclarator);
        }

        @Override
        public boolean containsMethodOfClassAndSignature(String classname, String signature) {
            return methodDeclaratorTextOfMethodOfTypeWithSignatureOrNull(classname, signature) != null;
        }

        @Override
        public @Nullable String methodDeclaratorTextOfMethodOfTypeWithSignatureOrNull(String type, String signature) {
            return simpleMethodDeclaratorToMethodDeclarator.get(JavaMethodDeclaratorUtil.simpleMethodDeclaratorText(type, signature));
        }

        @Override
        public boolean containsMethodOfClass(String classname) {
            return classes.contains(classname);
        }

        @Override
        public Stream<String> textStream() {
            return methodDeclarators.stream();
        }

        @Override
        public Iterator<JavaMethodDeclarator> iterator() {
            if (methods == null) {
                methods = stream().toList();
            }
            return methods.iterator();
        }
    }
}
