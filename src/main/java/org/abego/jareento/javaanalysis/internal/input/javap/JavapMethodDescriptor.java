package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.lang.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JavapMethodDescriptor {
    public final String className;
    public final String methodName;
    public final String[] parameters;
    public final String returnType;

    private static final Pattern LINE_PATTERN = Pattern.compile(
            "(?:(?:(?:Interface)?Method \"??(?:([\\[\\w$/]+;?)\"??\\.)?)" +
                    "|(?:InvokeDynamic #\\d+:))" +
                    "(\"?[\\w<>$]+\"?):\\(([^)]*)\\)(.+)*");

    private JavapMethodDescriptor(
            String className, String methodName, String[] parameters, String returnType) {
        this.className = className;
        this.methodName = methodName;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public static JavapMethodDescriptor parseFromJavapInvokeComment(String text) {
        Matcher m = LINE_PATTERN.matcher(text);
        if (!m.matches()) {
            throw new IllegalArgumentException("Error in method descriptor: " + text);
        }

        String className = "";
        if (m.group(1) != null) {
            String s = m.group(1);
            className = s.startsWith("[")
                    ? new JavapFieldAndReturnDescriptorParser(s).nextJavaType()
                    : StringUtil.slashesToDots(s);
        }

        String methodName = m.group(2);

        JavapFieldAndReturnDescriptorParser parser = new JavapFieldAndReturnDescriptorParser(m.group(3));
        List<String> paramTypes = new ArrayList<>();
        String s = parser.nextJavaType();
        while (!s.isEmpty()) {
            paramTypes.add(s);
            s = parser.nextJavaType();
        }
        String[] params = paramTypes.toArray(new String[0]);

        String returnType = new JavapFieldAndReturnDescriptorParser(m.group(4)).nextJavaType();

        return new JavapMethodDescriptor(className, methodName, params, returnType);
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
