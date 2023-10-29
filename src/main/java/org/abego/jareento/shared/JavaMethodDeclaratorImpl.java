package org.abego.jareento.shared;


import org.abego.jareento.javaanalysis.JavaMethodDeclarator;

public class JavaMethodDeclaratorImpl implements JavaMethodDeclarator {
    private final String text;
    private final int hashIndex;
    private final int colonIndex;
    private final int openParenthesisIndex;

    private JavaMethodDeclaratorImpl(String text) {
        this.text = text;
        hashIndex = text.indexOf('#');
        colonIndex = text.indexOf(':');
        openParenthesisIndex = text.indexOf('(');

        //TODO do we allow methodId with typeName (i.e. hashIndex <= 0)?
        if ((colonIndex <= hashIndex) ||
                (openParenthesisIndex <= hashIndex)) {
            throw new IllegalArgumentException("Invalid method declarator: " + text);
        }
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getSignatureText() {
        return hashIndex >= 0
                ? text.substring(hashIndex + 1, colonIndex)
                : text.substring(0, colonIndex);
    }

    @Override
    public String getMethodName() {
        return hashIndex >= 0
                ? text.substring(hashIndex + 1, openParenthesisIndex)
                : text.substring(0, openParenthesisIndex);
    }

    @Override
    public String getClassname() {
        return hashIndex >= 0 ? text.substring(0, hashIndex) : "";
    }

    @Override
    public String getSimpleClassname() {
        String typeName = getClassname();
        int i = typeName.lastIndexOf('.');
        return i >= 0 ? typeName.substring(i + 1) : typeName;
    }

    static JavaMethodDeclaratorImpl newJavaMethodDeclaratorImpl(String text) {
        return new JavaMethodDeclaratorImpl(text);
    }
}
