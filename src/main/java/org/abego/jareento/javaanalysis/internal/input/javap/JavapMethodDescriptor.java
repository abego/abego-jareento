package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.jareento.shared.SyntaxUtil;

import java.util.function.Function;

class JavapMethodDescriptor {
    public final String className;
    public final String methodName;
    public final String[] parameters;
    public final String returnType;

    public JavapMethodDescriptor(
            String className, String methodName, String[] parameters, String returnType) {
        this.className = className;
        this.methodName = methodName.equals("\"<init>\"")
                ? SyntaxUtil.simpleName(className) : methodName;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public String getSignature() {
        return getSignature(a -> a);
    }

    public String getSignature(Function<String, String> parameterMapping) {
        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append("(");
        boolean addSeparator = false;
        for (String p : parameters) {
            if (addSeparator) {
                result.append(", ");
            } else {
                addSeparator = true;
            }
            result.append(parameterMapping.apply(p));
        }
        result.append(")");
        return result.toString();
    }
}
