package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.abego.jareento.javaanalysis.internal.JavaAnalysisProjectStateBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.abego.jareento.util.JavaLangUtil.rawName;

class JavaClassForJavaAnalysisProjectStateUtil {
    JavaClassForJavaAnalysisProjectStateUtil() {
        throw new MustNotInstantiateException();
    }

    public static void addClassToJavaAnalysisProjectState(Class<?> clazz, JavaAnalysisProjectStateBuilder builder) {

        processClass(clazz, builder);
    }

    private static void processClass(Class<?> clazz, JavaAnalysisProjectStateBuilder builder) {

        String classname = clazz.getTypeName();

        builder.addClass(classname);
        builder.setClassIsDeclared(classname, true);

        Type superClass = clazz.getGenericSuperclass();
        if (superClass != null) {
            builder.addTypeExtends(classname, rawName(superClass.getTypeName()));
        }

        for (Class<?> type : clazz.getInterfaces()) {
            builder.addTypeImplements(classname, rawName(type.getTypeName()));
        }

        for (Method method : clazz.getDeclaredMethods()) {
            String signature = method.getName() +
                    '(' +
                    Arrays.stream(method.getParameters())
                            .map(p -> p.getType().getTypeName())
                            .collect(Collectors.joining(", ")) +
                    ')';
            builder.addMethod(classname, signature, method.getReturnType()
                    .getTypeName());
        }

        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            processClass(innerClass, builder);
        }
    }
}
