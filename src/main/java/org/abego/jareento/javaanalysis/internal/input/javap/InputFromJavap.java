package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.lang.ArrayUtil;
import org.abego.commons.lang.ClassLoaderUtil;
import org.abego.commons.lang.SeparatedItemScanner;
import org.abego.commons.progress.ProgressWithRange;
import org.abego.commons.progress.Progresses;
import org.abego.jareento.base.WithId;
import org.abego.jareento.javaanalysis.JavaAnalysisAPI;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaType;
import org.abego.jareento.javaanalysis.JavaMethodCalls;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisInternalFactories;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectInput;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectInternal;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectStateBuilder;
import org.abego.jareento.shared.commons.progress.ProgressWithRangeListenerWithStringConsumer;
import org.abego.jareento.util.JavaLangUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Logger.getLogger;
import static org.abego.commons.lang.SeparatedItemScanner.newSeparatedItemScanner;
import static org.abego.jareento.shared.SyntaxUtil.simpleName;
import static org.abego.jareento.util.JavaLangUtil.rawName;

/**
 * Extracts data from a file generated by "javap".
 * <p>
 * To resolve external references a list of jar files can be provided.
 * <p>
 * (see also {@link JavapParser})
 */
public class InputFromJavap implements JavaAnalysisProjectInput {
    private static final Logger LOGGER = getLogger(InputFromJavap.class.getName());
    private static final String OBJECT_CLASS_NAME = Object.class.getName();

    private final ProgressWithRange.Listener progressListener;
    private final File file;
    private final File[] jarFiles;

    private record QualifiedMethodSpecifier(
            String declaringType,
            String methodSignature,
            String returnType) {
    }


    private class MyJavapParserEventHandler implements JavapParser.EventHandler {
        private final JavaAnalysisProjectStateBuilder builder;
        private final ProgressWithRange progressWithRange;
        private final Map<String, String> typeParameters;
        private final Map<String, String> methodTypeParameters;
        private final Set<String> callsWithUndefinedScope;
        private String currentClassfile;
        private QualifiedMethodSpecifier currentMethodSpecifier =
                new QualifiedMethodSpecifier("", "", "");
        private int classIndex;

        private MyJavapParserEventHandler(JavaAnalysisProjectStateBuilder builder, ProgressWithRange progressWithRange) {
            this.builder = builder;
            this.progressWithRange = progressWithRange;
            this.classIndex = 0;
            this.currentClassfile = "";
            this.typeParameters = new HashMap<>();
            this.methodTypeParameters = new HashMap<>();
            this.callsWithUndefinedScope = new HashSet<>();
        }

        @Override
        public void onClassfile(String classfile) {
            currentClassfile = classfile;
        }


        @Override
        public void onClass(String typeName, String access, String modifier, String type, String[] extendedTypes, String[] implementedTypes) {
            progressWithRange.update(classIndex++, typeName);

            String rawClassname = rawName(typeName);
            builder.addClass(rawClassname);
            builder.addClassInClassFile(rawClassname, currentClassfile);
            builder.setClassIsDeclared(rawClassname, true);

            boolean isInterface = type.equals("interface");
            if (isInterface) {
                builder.setIsInterfaceOfClass(rawClassname, true);
            }
            updateTypeParameters(typeParameters, typeName);
            methodTypeParameters.clear();
            if (extendedTypes.length != 0) {
                for (String extendedType : extendedTypes) {
                    //TODO shall we also include generic type information?
                    builder.addTypeExtends(rawClassname, rawName(extendedType));
                }
            } else if (!isInterface) {
                // when no 'extends' is defined extend for object (if not interface)
                //TODO shall we also include generic type information?
                builder.addTypeExtends(rawClassname, OBJECT_CLASS_NAME);
            }

            for (String t : implementedTypes) {
                //TODO shall we also include generic type information?
                builder.addTypeImplements(rawClassname, rawName(t));
            }
        }

        @Override
        public void onMethod(String typeName, String methodName, String access, String modifier, String returnType, String parameters, String exceptions, String typeParametersOfMethod) {
            currentMethodSpecifier = getQualifiedMethodSpecifier(
                    typeName, methodName, returnType, parameters, typeParametersOfMethod);
            builder.addMethod(
                    currentMethodSpecifier.declaringType(),
                    currentMethodSpecifier.methodSignature(),
                    currentMethodSpecifier.returnType());
        }

        @Override
        public void onFlags(String typeName, String methodName, String parameters, String returnType, String[] flags) {
            // For now, we are only interested in "synthetic *methods*" (not types)
            if (!methodName.isEmpty() && ArrayUtil.contains(flags, "ACC_SYNTHETIC")) {
                String methodId = builder.getMethodId(
                        currentMethodSpecifier.declaringType(),
                        currentMethodSpecifier.methodSignature(),
                        currentMethodSpecifier.returnType());
                builder.setMethodIsSynthetic(methodId, true);
            }
        }

        private QualifiedMethodSpecifier getQualifiedMethodSpecifier(String typeName, String methodName, String returnType, String parameters, String typeParametersOfMethod) {
            updateTypeParameters(methodTypeParameters, typeParametersOfMethod);
            if (!typeParametersOfMethod.isEmpty()) {
                parameters = bindTypeParameter(methodTypeParameters, parameters);
                returnType = bindTypeParameter(methodTypeParameters, returnType);
            }
            if (!typeParameters.isEmpty()) {
                parameters = bindTypeParameter(typeParameters, parameters);
                returnType = bindTypeParameter(typeParameters, returnType);
            }
            //TODO pass typeParametersOfMethod to builder
            String rawTypeName = rawName(typeName);
            String methodSignature = signatureFromMethodNameAndParameters(methodName, parameters);
            String rawReturnType = rawName(returnType);
            return new QualifiedMethodSpecifier(rawTypeName, methodSignature, rawReturnType);
        }

        @Override
        public void onInstruction(String className, String methodName, String parameters, String returnType, int offset, String mnemonic, String arguments, String comment) {
            if (mnemonic.startsWith("invoke")) {
                JavapMethodDescriptor calledMethod = JavapUtil.parseFromJavapInvokeComment(comment, className);
                String calledSignature = calledMethod.getSignature(JavaLangUtil::rawName);
                String callingMethodSignature = signatureFromMethodNameAndParameters(methodName, parameters);
                if (!methodTypeParameters.isEmpty()) {
                    returnType = bindTypeParameter(methodTypeParameters, returnType);
                }
                if (!typeParameters.isEmpty()) {
                    returnType = bindTypeParameter(typeParameters, returnType);
                }
                //TODO shall we also include generic type information?
                String callScopeTypename = rawName(calledMethod.className);
                String callingMethodTypename = rawName(className);
                boolean callerIsConstructor = 
                        simpleName(callingMethodTypename).equals(methodName);
                String callingMethodReturnType = callerIsConstructor
                        ? "void" : rawName(returnType);
                String methodCallId = builder.addMethodCall(
                        mnemonic,
                        callScopeTypename,
                        calledSignature,
                        callingMethodTypename,
                        callingMethodSignature,
                        callingMethodReturnType,
                        Integer.toString(offset));
                if (callScopeTypename.isEmpty() && !mnemonic.equals("invokedynamic")) {
                    callsWithUndefinedScope.add(methodCallId);
                }
            }
        }

        @Override
        public void onEnd() {
            // Some operations require information regarding inheritance 
            // hierarchy, existing method definitions etc.
            //
            // This information is now available in the builder after we have
            // processed the complete javap output. Therefore, we now create 
            // "intermediate" JavaAnalysisProjects from the builder to access 
            // this information. 
            // 
            // The "old" builder will still be used by the later operation, 
            // to include the additional information.
            declareUndeclaredTypes(buildJavaAnalysisProject(builder));
            updateMethodCallsWithUndefinedScope(buildJavaAnalysisProject(builder));
            setMethodCallBaseScope(buildJavaAnalysisProject(builder));
        }

        private void setMethodCallBaseScope(JavaAnalysisProjectInternal project) {
            MethodResolver methodResolver = new MethodResolver(project);

            JavaMethodCalls javaMethodCalls = project.getMethodCalls();
            int n = javaMethodCalls.getSize();
            ProgressWithRange progress = Progresses.createProgressWithRange(
                    "Setting method call base scopes", n, progressListener);
            int[] i = new int[]{0};
            javaMethodCalls.idStream().forEach(methodCallId -> {
                progress.update(i[0], "methodCallId");
                i[0]++;

                String currentScope = project.scopeOfMethodCall(methodCallId);
                if (!currentScope.isEmpty()) {
                    String signature = project.signatureOfMethodCall(methodCallId);
                    @Nullable
                    String methodId = methodResolver.idOfMethodForSignatureAndClassOrNull(
                            signature, currentScope);
                    if (methodId != null) {
                        String baseScope = project.classOfMethod(methodId);
                        builder.setMethodCallBaseScope(methodCallId, baseScope);
                    }
                }
            });
            progress.close();
        }

        private void updateMethodCallsWithUndefinedScope(JavaAnalysisProjectInternal project) {

            MethodResolver methodResolver = new MethodResolver(project);
            int n = callsWithUndefinedScope.size();
            int i = 0;
            ProgressWithRange progress2 = Progresses.createProgressWithRange(
                    "Updating undefined method call scopes", n, progressListener);
            for (String methodCallId : callsWithUndefinedScope) {
                progress2.update(i++, methodCallId);
                try {
                    String methodId = methodResolver.idOfMethodForMethodCall(methodCallId);
                    String scope = project.classOfMethod(methodId);
                    builder.setMethodCallScope(methodCallId, scope);
                } catch (Exception e) {
                    //TODO: fix this
                    String typeName = project.classContainingMethodCall(methodCallId);
                    String signature = project.signatureOfMethodCall(methodCallId);
                    LOGGER.log(Level.SEVERE, e, () ->
                            String.format("Error when resolving method with signature `%s` in class `%s`",
                                    signature, typeName));
                }
            }
            progress2.close();
        }

        private void declareUndeclaredTypes(JavaAnalysisProject project) {

            List<String> undeclared = getUndeclaredTypes(project);

            ClassLoader classLoader = ClassLoaderUtil.classLoaderUsingJars(jarFiles);
            Set<String> missingTypes = new HashSet<>();
            while (undeclared.size() > 0) {
                List<String> oldUndeclared = undeclared;
                for (String typeName : undeclared) {
                    if (missingTypes.contains(typeName)) {
                        continue;
                    }
                    try {
                        String name = typeName;
                        while (name.endsWith("[]")) {
                            name = name.substring(0, name.length() - 2);
                        }
                        Class<?> clazz = classLoader.loadClass(name);
                        addClassToJavaAnalysisProjectState(clazz, builder);
                    } catch (Throwable e) {
                        missingTypes.add(typeName);
                        //TODO
                        System.err.println("Class not found: " + typeName + " (" + e.getMessage() + ")");
                    }
                }

                // build the project again, as we added new types (at least java.lang.Object)
                project = buildJavaAnalysisProject(builder);
                undeclared = getUndeclaredTypes(project);

                // we need to re-run the loop as adding types may have
                // added new dependencies we now need to load.

                // Safety Check: When the last run did not change the list of
                // undeclared types we exit to avoid the endless loop.
                if (oldUndeclared.equals(undeclared)) {
                    //TODO: add some warning or so we exited the loop "abnormally".
                    break;
                }
            }
        }
    }

    private JavaAnalysisProjectInternal buildJavaAnalysisProject(JavaAnalysisProjectStateBuilder builder) {
        return JavaAnalysisInternalFactories.newJavaAnalysisProject(builder.build());
    }

    private static String signatureFromMethodNameAndParameters(String methodName, String parameters) {
        return methodName + "(" + JavaLangUtil.rawParameters(parameters) + ")";
    }
    
    private InputFromJavap(
            File file, File[] jarFiles, Consumer<String> progress, 
            @SuppressWarnings("unused") JavaAnalysisAPI javaAnalysisAPI) {
        this.file = file;
        this.jarFiles = jarFiles;
        this.progressListener = ProgressWithRangeListenerWithStringConsumer.
                newProgressListenerWithStringConsumer(progress);
    }

    public static InputFromJavap newInputFromJavap(
            File file, File[] jarFiles, Consumer<String> progress, JavaAnalysisAPI javaAnalysisAPI) {
        return new InputFromJavap(file, jarFiles, progress, javaAnalysisAPI);
    }

    @Override
    public void feed(JavaAnalysisProjectStateBuilder builder, Consumer<String> problemConsumer) {
        ProgressWithRange progress = Progresses.createProgressWithRange(
                "Importing disassembled classes...", Integer.MAX_VALUE, progressListener);
        JavapParser javapParser = JavapParser.newJavapParser();
        MyJavapParserEventHandler handler =
                new MyJavapParserEventHandler(builder, progress);
        javapParser.parseFile(file, handler);
        progress.close();
    }

    private static String bindTypeParameter(Map<String, String> typeParameters, String parameters) {
        if (parameters.isEmpty()) {
            return "";
        }

        return JavaLangUtil.parseParameters(parameters).stream()
                .map(s -> {
                    @Nullable String bound = typeParameters.get(s);
                    return bound != null ? bound : s;
                }).collect(Collectors.joining(", "));
    }

    private static void updateTypeParameters(Map<String, String> genericTypes, String typeName) {
        genericTypes.clear();

        int start = typeName.indexOf('<');
        int end = typeName.lastIndexOf('>');
        if (start >= 0 && end > start) {
            String text = typeName.substring(start + 1, end);
            SeparatedItemScanner scanner = newSeparatedItemScanner(text);
            String nextItem = scanner.nextItem();
            while (!nextItem.isEmpty()) {
                String name = nextItem;
                String bound = OBJECT_CLASS_NAME;
                nextItem = scanner.nextItem();
                if (nextItem.equals("extends")) {
                    bound = scanner.nextItem();
                    nextItem = scanner.nextItem();
                }
                genericTypes.put(name, bound);
            }
        }
    }

    public static void addClassToJavaAnalysisProjectState(Class<?> clazz, JavaAnalysisProjectStateBuilder builder) {

        addClassToJavaAnalysisProjectStateHelper(clazz, builder);
    }

    private static void addClassToJavaAnalysisProjectStateHelper(Class<?> clazz, JavaAnalysisProjectStateBuilder builder) {

        String typeName = clazz.getTypeName();

        builder.addClass(typeName);
        builder.setClassIsDeclared(typeName, true);

        Type superClass = clazz.getGenericSuperclass();
        if (superClass != null) {
            builder.addTypeExtends(typeName, rawName(superClass.getTypeName()));
        }

        for (Class<?> type : clazz.getInterfaces()) {
            builder.addTypeImplements(typeName, rawName(type.getTypeName()));
        }

        for (Method method : clazz.getDeclaredMethods()) {
            String signature = method.getName() +
                    '(' +
                    Arrays.stream(method.getParameters())
                            .map(p -> p.getType().getTypeName())
                            .collect(Collectors.joining(", ")) +
                    ')';
            builder.addMethod(typeName, signature, method.getReturnType()
                    .getTypeName());
        }

        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            addClassToJavaAnalysisProjectStateHelper(innerClass, builder);
        }
    }
    
    private static final Set<String> primitiveTypes = new HashSet<>(
            Arrays.asList("boolean", "byte", "char", "double", "float", "int", "long", "short", "void"));

    //TODO to project API?
    private static boolean isPrimitiveType(String type) {
        return primitiveTypes.contains(type);
    }

    //TODO: to project API?
    private static boolean isUndeclared(JavaType javaType, JavaAnalysisProject project) {
        String fullName = javaType.getId();
        return !project.hasTypeWithName(fullName) && !isPrimitiveType(fullName);
    }

    private static List<String> getUndeclaredTypes(JavaAnalysisProject project) {
        return project.getTypes().stream()
                .filter(c -> isUndeclared(c, project))
                .map(WithId::getId)
                .collect(Collectors.toList());
    }
}
