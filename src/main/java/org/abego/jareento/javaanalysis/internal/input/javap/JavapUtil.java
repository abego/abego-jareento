package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.lang.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JavapUtil {

    private static class JavapFieldAndReturnDescriptorParser {
        private final String text;
        private int pos;
        private final int endIndex;

        JavapFieldAndReturnDescriptorParser(String text, int startIndex, int endIndex) {
            this.text = text;
            this.pos = startIndex;
            this.endIndex = endIndex;
        }

        String nextJavaType() {
            if (pos >= endIndex) {
                return "";
            }
            char c = text.charAt(pos++);
            return switch (c) {
                case 'B' -> "byte";
                case 'C' -> "char";
                case 'D' -> "double";
                case 'F' -> "float";
                case 'I' -> "int";
                case 'J' -> "long";
                case 'L' -> parseLJavaType();
                case 'S' -> "short";
                case 'V' -> "void";
                case 'Z' -> "boolean";
                case '[' -> nextJavaType() + "[]";
                default ->
                        throw new IllegalArgumentException("Unexpected type character: " + c);
            };
        }

        private String parseLJavaType() {
            int startPos = pos;
            while (text.charAt(pos) != ';') {
                pos++;
            }
            return StringUtil.slashesToDots(text.substring(startPos, pos++));
        }
    }
    
    public static ParameterAndReturnTypes parseJavapDescriptor(String text) {
        if (text.isEmpty()) {
            throw new IllegalArgumentException("text must not be empty");
        }
        
        boolean isDescriptorOfMethod = text.charAt(0) == '(';
        if (isDescriptorOfMethod) {
            int end = text.indexOf(')');
            if (end < 0) {
                throw new IllegalArgumentException(
                        "text missing closing ')': %s"
                                .formatted(text));
            }
            return new ParameterAndReturnTypes(
                    parseJavapParameterTypes(text, 1, end),
                    parseJavapType(text, end + 1, text.length()));
        } else {
            return new ParameterAndReturnTypes(null, parseJavapType(text));
        }
    }

    public static String[] parseJavapParameterTypes(
            String text, int startIndex, int endIndex) {

        JavapFieldAndReturnDescriptorParser parser =
                new JavapFieldAndReturnDescriptorParser(
                        text, startIndex, endIndex);
        List<String> paramTypes = new ArrayList<>();
        String s = parser.nextJavaType();
        while (!s.isEmpty()) {
            paramTypes.add(s);
            s = parser.nextJavaType();
        }
        return paramTypes.toArray(new String[0]);
    }

    public static String[] parseJavapParameterTypes(String text) {
        return parseJavapParameterTypes(
                text, 0, text.length());
    }


    public static String parseJavapType(
            String text, int startIndex, int endIndex) {
        return new JavapFieldAndReturnDescriptorParser(text, startIndex, endIndex)
                .nextJavaType();
    }

    public static String parseJavapType(String text) {
        return parseJavapType(text, 0, text.length());
    }

    private static final Pattern INVOKE_COMMENT_PATTERN = Pattern.compile(
            "(?:(?:(?:Interface)?Method \"??(?:([\\[\\w$/]+;?)\"??\\.)?)" +
                    "|(?:InvokeDynamic #\\d+:))" +
                    "(\"?[\\w<>$]+\"?):\\(([^)]*)\\)(.+)*");


    /**
     3: invokestatic  #19                 // Method java/lang/Enum.valueOf:(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;

     * "// Method java/lang/Enum.valueOf:(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;"
     */
    public static JavapMethodDescriptor parseFromJavapInvokeComment(String text, String classNameDefault) {
        Matcher m = INVOKE_COMMENT_PATTERN.matcher(text);
        if (!m.matches()) {
            throw new IllegalArgumentException("Error in method descriptor: " + text);
        }

        String className = classNameDefault;
        if (m.group(1) != null) {
            String s = m.group(1);
            className = s.startsWith("[")
                    ? JavapUtil.parseJavapType(s)
                    : StringUtil.slashesToDots(s);
        }

        String methodName = m.group(2);

        String[] params = JavapUtil.parseJavapParameterTypes(m.group(3));

        String returnType = JavapUtil.parseJavapType(m.group(4));

        return new JavapMethodDescriptor(className, methodName, params, returnType);
    }


}
