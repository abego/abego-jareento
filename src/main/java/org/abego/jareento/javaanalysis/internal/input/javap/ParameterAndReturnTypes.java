package org.abego.jareento.javaanalysis.internal.input.javap;

import org.eclipse.jdt.annotation.Nullable;

record ParameterAndReturnTypes(
        String @Nullable [] parameterTypes, String returnType) {
}
