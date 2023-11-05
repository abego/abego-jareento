package org.abego.jareento.javaanalysis;

import org.junit.jupiter.api.Test;

import static org.abego.jareento.javaanalysis.JavaMethodDeclaratorsBuilder.newJavaMethodDeclaratorsBuilder;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JavaMethodDeclaratorsBuilderTest {
    @Test
    void build$empty() {
        JavaMethodDeclaratorsBuilder builder = newJavaMethodDeclaratorsBuilder();
        JavaMethodDeclarators result = builder.build();
        
        assertFalse(result.iterator().hasNext());
    }
}
