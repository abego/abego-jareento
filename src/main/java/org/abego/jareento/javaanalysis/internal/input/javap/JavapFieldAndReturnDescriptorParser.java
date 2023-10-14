package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.lang.StringUtil;

class JavapFieldAndReturnDescriptorParser {
    private final String text;
    private int pos = 0;

    JavapFieldAndReturnDescriptorParser(String text) {
        this.text = text;
    }

    String nextJavaType() {
        if (pos >= text.length()) {
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
