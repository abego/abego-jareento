package org.abego.jareento.util;

import org.abego.commons.lang.SeparatedItemScanner;
import org.abego.commons.lang.exception.MustNotInstantiateException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.abego.commons.lang.SeparatedItemScanner.newSeparatedItemScanner;

public final class JavaLangUtil {
    JavaLangUtil() {
        throw new MustNotInstantiateException();
    }

    public static String[] parameterTypesOfSignature(String signature) {
        int s = signature.indexOf('(');
        int e = signature.indexOf(')');
        String parameterTypes = signature.substring(s + 1, e);
        // doWithIndex(com.synchrony.cl.vast.base.core.Void2Block<V, java.lang.Integer>)
        // we cannot just split the text by ',', as generic types may contain
        // commas, too. E.g.: m(com.example.Void2Block<V, java.lang.Integer>)
        return splitTextWithGenericsAtCommas(parameterTypes);
    }

    private static final Pattern COMMA_LESS_GREATER = Pattern.compile("[,<>]");

    static String[] splitTextWithGenericsAtCommas(String text) {
        List<String> result = new ArrayList<>();
        Matcher m = COMMA_LESS_GREATER.matcher(text);
        int i = 0;
        int level = 0;
        while (m.find()) {
            switch (m.group()) {
                case "," -> {
                    if (level == 0) {
                        result.add(text.substring(i, m.start()).trim());
                        i = m.end();
                    }
                }
                case "<" -> level++;
                case ">" -> level--;
                default ->
                        throw new IllegalStateException("Invalid case: " + m.group());
            }
        }
        if (i < text.length()) {
            result.add(text.substring(i).trim());
        }

        return result.toArray(new String[0]);
    }

    /**
     * Returns the non-empty "items" of the comma/space-separated list in
     * {@code parameters}, making sure not to split inside of "&lt;...&gt;" blocks.
     */
    public static List<String> parseParameters(String parameters) {
        SeparatedItemScanner scanner = newSeparatedItemScanner(parameters);
        List<String> result = new ArrayList<>();
        String nextItem = scanner.nextItem();
        while (!nextItem.isEmpty()) {
            result.add(nextItem);
            nextItem = scanner.nextItem();
        }
        return result;
    }

    public static String nameOfSignature(String signature) {
        int i = signature.indexOf('(');
        if (i <= 0) {
            throw new IllegalArgumentException("Not a valid signature (no '(' found): " + signature);
        }
        return signature.substring(0, i);
    }

    public static List<String> parametersOfSignature(String signature) {

        int start = signature.indexOf('(');
        if (start <= 0) {
            throw new IllegalArgumentException("Not a valid signature (no '(' found): " + signature);
        }
        int signatureLength = signature.length();
        if (signature.charAt(signatureLength - 1) != ')') {
            throw new IllegalArgumentException("Not a valid signature (does not end with ')'): " + signature);
        }
        return parseParameters(signature.substring(start + 1, signatureLength - 1));
    }

    public static String rawName(String fullName) {
        int i = fullName.indexOf('<');
        return i >= 0 ? fullName.substring(0, i) : fullName;
    }

    public static String rawNameNoArray(String fullName) {
        return rawName(fullName.replaceAll("\\[]", ""));
    }

    public static String rawParameters(String parameter) {
        return parseParameters(parameter).stream()
                .map(JavaLangUtil::rawName)
                .collect(Collectors.joining(", "));
    }
}
