package org.abego.jareento.javaanalysis.internal.input.javap;

import org.abego.commons.lang.StringUtil;
import org.abego.jareento.base.JareentoException;
import org.abego.jareento.javaanalysis.JavaAnalysisProject;
import org.abego.jareento.javaanalysis.JavaClasses;
import org.abego.jareento.javaanalysis.JavaMethods;
import org.abego.jareento.util.JavaLangUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.abego.jareento.util.JavaLangUtil.parametersOfSignature;

class MethodResolver {
    private static final String OBJECT_CLASS_NAME = Object.class.getName();
    private final JavaAnalysisProject project;

    private final Map<String, Set<String>> directlyInheritsFrom = new HashMap<>();
    private final Map<String, Set<String>> inheritsFrom = new HashMap<>();

    MethodResolver(JavaAnalysisProject project) {
        this.project = project;

        initDirectInheritance(project);
    }

    /**
     * Returns the id of the method that would be called by the MethodCall with
     * the given {@code methodCallId}, starting the method lookup at the class
     * containing the method call.
     * <p>
     * Throws an exception when the method cannot be found.
     */
    public String idOfMethodForMethodCall(String methodCallId) {
        String classname = project.classContainingMethodCall(methodCallId);
        return idOfMethodForMethodCall(methodCallId, classname);
    }

    /**
     * Returns the id of the method that would be called by the MethodCall with
     * the given {@code methodCallId}, starting the method lookup at the class
     * with the given {@code classname}.
     * <p>
     * Throws an exception when the method cannot be found.
     */
    public String idOfMethodForMethodCall(String methodCallId, String classname) {
        String signature = project.signatureOfMethodCall(methodCallId);
        String methodId = idOfMethodForSignatureAndClassOrNull(signature, classname);
        if (methodId == null) {
            throw new JareentoException(String.format(
                    "No method found with signature `%s` (in or below `%s`). Called by %s.",
                    signature, classname, methodCallId));
        }
        return methodId;
    }

    @Nullable
    public String idOfMethodForSignatureAndClassOrNull(String signature, String classname) {
        @Nullable
        String methodId = resolveSignatureToMethodIdOrNull(signature, classname);
        if (methodId == null && signature.startsWith("\"<init>\"")) {
            // check for the constructor (i.e. replace the quoted <init> with the classname)
            String sig2 = classname + signature.substring(8);
            methodId = resolveSignatureToMethodIdOrNull(sig2, classname);
        }
        if (methodId == null && signature.endsWith("[])")) {
            // look for a varargs definition (i.e. replace the '[]' with '...')
            String sig2 = StringUtil.prefix(signature, -3) + "...)";
            methodId = resolveSignatureToMethodIdOrNull(sig2, classname);
        }
        return methodId;
    }

    private Set<String> getInheritedTypes(String classname) {
        Set<String> result = inheritsFrom.get(classname);
        if (result == null) {
            result = calcInheritedTypes(classname);
            inheritsFrom.put(classname, result);
        }
        return result;
    }

    /**
     * Compares the fully qualified (raw) reference types {@code typeA} and
     * {@code typeB} and returns
     * <ul>
     *     <li>{@code 0} if both are equal, </li>
     *     <li>{@code -1} if {@code typeA} is less specific than {@code typeB}
     *     (i.e. one can assign values of {@code typeB} to {@code typeA} fields), </li>
     *     <li>{@code 1} if {@code typeA} is more specific than {@code typeB}
     *     (i.e. one can assign values of {@code typeA} to {@code typeB} fields),</li>
     *     <li>{@code null} if {@code typeA} and {@code typeB} are not related
     *     to each other</li>
     * </ul>
     */
    @Nullable
    private Integer compareType(String typeA, String typeB) {
        //TODO: check if types are raw and not primitive

        // equal types
        if (typeA.equals(typeB)) {
            return 0;
        }

        // Shortcuts: one of the types is Object (no need to traverse the inheritance)
        if (typeA.equals(OBJECT_CLASS_NAME)) {
            return -1;
        }
        if (typeB.equals(OBJECT_CLASS_NAME)) {
            return 1;
        }

        // use the type inheritance to determine what is more specific
        if (getInheritedTypes(typeB).contains(typeA)) {
            // typeA is less specific than typeB
            return -1;
        }
        if (getInheritedTypes(typeA).contains(typeB)) {
            // typeA is more specific than typeB
            return 1;
        }

        // types are not related to each other
        return null;
    }

    /**
     * Compares the fully qualified (raw) reference types in {@code typesA} to
     * corresponding types in {@code typesB} (index by index) and returns
     * <ul>
     *     <li>{@code 0} if all types in {@code typesA} are equal to the corresponding types in {@code typesB}, </li>
     *     <li>{@code -1} if all types in {@code typesA} are less specific than the corresponding types in {@code typesB}
     *     (i.e. one can assign values of {@code typesB} to {@code typesA} fields), </li>
     *     <li>{@code -2} if some types in {@code typesA} are less specific than the corresponding types in {@code typesB}
     *     and some types in {@code typesA} are equal to the corresponding types in {@code typesB}, </li>
     *     <li>{@code 1} if all types in {@code typesA} are more specific than the corresponding types in {@code typesB}
     *     (i.e. one can assign values of {@code typesA} to {@code typesB} fields),</li>
     *     <li>{@code 2} if some types in {@code typesA} are more specific than the corresponding types in {@code typesB}
     *     and some types in {@code typesA} are equal to the corresponding types in {@code typesB}, </li>
     *     <li>{@code null} if {@code typesA} and {@code typesB} are not related
     *     to each other, or some types in {@code typesA} are more specific than the corresponding types in {@code typesB} and
     *     some types in {@code typesA} is less specific than the corresponding types in {@code typesB}</li>
     * </ul>
     */
    @Nullable
    private Integer compareTypes(List<String> typesA, List<String> typesB) {
        int count = typesA.size();
        if (count != typesB.size()) {
            throw new IllegalStateException("Number of types don't match");
        }
        if (count == 0) {
            return 0;
        }

        Integer result = null;
        for (int i = 0; i < count; i++) {
            @Nullable Integer v = compareType(typesA.get(i), typesB.get(i));
            if (v == null) {
                return null;
            }

            // v != null
            int value = v;
            if (result == null) {
                // first setter
                result = value;
            } else {
                // result != null

                // possible combinations:
                //
                //    |  old result  | value | new result
                // ------------------+-------+-----------
                //  E |       0      |  -1   |     -2
                //  A |       0      |   0   |      0
                //  E |       0      |   1   |      2
                //  A |      -1      |  -1   |     -1
                //  D |      -1      |   0   |     -2
                //  B |      -1      |   1   |    null
                //  C |      -2      |  -1   |     -2
                //  C |      -2      |   0   |     -2
                //  B |      -2      |   1   |    null
                //  B |       1      |  -1   |    null
                //  D |       1      |   0   |      2
                //  A |       1      |   1   |      1
                //  B |       2      |  -1   |    null
                //  C |       2      |   0   |      2
                //  C |       2      |   1   |      2
                //
                //  A, B, C, D, E : correlates to code below

                if (result == value) {
                    continue; // A
                }
                // When result and value have different signs things are incompatible
                if ((result < 0 && value > 0) || (result > 0 && value < 0)) {
                    return null; // B
                }

                // for result == 2 or -2 the result will not change, 
                // independent of the current value. 
                if (result != 2 && result != -2) { // C
                    if (value == 0) {
                        result *= 2; // D
                    } else {
                        result = value * 2; // E
                    }
                }
            }
        }
        return result;
    }

    private Set<String> calcInheritedTypes(String classname) {
        Set<String> cached = inheritsFrom.get(classname);
        if (cached != null) {
            return cached;
        }
        Set<String> result = new HashSet<>();
        Set<String> directParents = directlyInheritsFrom.get(classname);
        if (directParents != null) {
            for (String parent : directParents) {
                result.addAll(calcInheritedTypes(parent));
            }
            result.addAll(directParents);
        }
        return result;
    }

    private void initDirectInheritance(JavaAnalysisProject project) {
        project.getClasses().idStream().forEach(classname -> {
            JavaClasses types = project.extendedTypes(classname)
                    .unitedWith(project.implementedInterfaces(classname));
            for (String supertype : types.getNames()) {
                directlyInheritsFrom
                        .computeIfAbsent(classname, s -> new HashSet<>())
                        .add(supertype);
            }
        });
    }

    @Nullable
    private String resolveSignatureToMethodIdOrNull(String signature, String classname) {
        @Nullable
        String result = resolveSignatureToMethodOfSpecificClassOrNull(signature, classname);
        if (result != null) {
            return result;
        }

        // search in the inherited types for a matching method
        JavaClasses extendedTypes = project.extendedTypes(classname);
        JavaClasses interfaces = project.implementedInterfaces(classname);

        JavaClasses types = extendedTypes.unitedWith(interfaces);

        if (types.getSize() == 0) {
            // Reached the end of the hierarchy (e.g. Object or some interface) 
            return null;

        } else {
            for (String typename : types.getNames()) {
                @Nullable String id = resolveSignatureToMethodIdOrNull(signature, typename);
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }

    @Nullable
    private String resolveSignatureToMethodOfSpecificClassOrNull(
            String signature, String classname) {
        // To resolve the signature in the context of the class with the given
        // classname we first select those methods from the class that are
        // possible "candidates" to match the signature. 
        // 
        // To be a candidate a method must have the same name as the name used 
        // in the signature and the same number of parameters as the signature.
        JavaMethods methods = project.methodsOfClass(classname);
        Map<String, List<String>> candidates =
                getMethodCandidatesForSignatureIgnoringTypes(methods, signature);

        // For these method candidates we check if the parameter types of the
        // method match the types used in the signature. We want the method 
        // with the most specific parameter types that is still compatible with
        // the signature.
        List<String> signatureParameters = parametersOfSignature(signature);
        @Nullable String idOfBestMethod = null;
        List<String> parametersOfBestMethod = null;
        for (Map.Entry<String, List<String>> entry : candidates.entrySet()) {
            String methodId = entry.getKey();
            List<String> parameters = entry.getValue();
            Integer value = compareTypes(parameters, signatureParameters);
            if (value != null) {
                // TODO: for now we only check equality. 
                //  Considering type hierarchy had problems with "un-comparable" types
                //  Do we need more than equality?
                if (value == 0) {
                    // the parameters of the current entry are as specific or
                    // more specific than the signature. This would be a 
                    // possible result. 
                    //
                    // Let's check with previous results...
                    if (idOfBestMethod == null) {
                        // nothing found yet. Use it.
                        idOfBestMethod = methodId;
                        parametersOfBestMethod = parameters;
                    } else {
                        // There is already some result. Let's compare both
                        Integer compareResult = compareTypes(parameters, parametersOfBestMethod);
                        if (compareResult == null) {
                            // This should not happen.
                            throw new JareentoException(String.format(
                                    "Internal error when comparing the parameter types of %s and %s", methodId, idOfBestMethod));
                        }
                        if (compareResult == 0) {
                            // The parameter types of both methods are equal.
                            // In that case we choose the method with the 
                            // most specific return type.
                            // (This is something special because we work on the
                            // bytecode. In the bytecode a class may have 
                            // multiple methods with the same signature but 
                            // different return types. The less specific ones
                            // simply call the more specific one, with some 
                            // additional cast checks. In the Java Source code
                            // this is not visible.)
                            String oldMethodReturnType = project.returnTypeOfMethod(idOfBestMethod);
                            String newMethodReturnType = project.returnTypeOfMethod(methodId);
                            compareResult = compareType(newMethodReturnType, oldMethodReturnType);
                            if (compareResult == null) {
                                // Ignore incompatible compares
                                continue;
                            }
                            // as we used the same variable `compareResult` 
                            // we can now fall through to the outer test that
                            // will decide if we found something better.
                        }
                        if (compareResult > 0) {
                            // found a better method
                            idOfBestMethod = methodId;
                            parametersOfBestMethod = parameters;
                        }
                    }
                }
            }
        }
        return idOfBestMethod;
    }

    /**
     * Returns all methods that have the same name as the name of the
     * {@code methodSignature} and the same number of parameters, as a map with
     * the method's id as key and the method's parameter types as value.
     * <p>
     * Note: the result may also include methods that don't have parameter type
     * compatible with the types of the methodSignature. Finding the "proper"
     * method out of these candidates must be done in a separate step.
     */
    private Map<String, List<String>> getMethodCandidatesForSignatureIgnoringTypes(
            JavaMethods methods, String methodSignature) {
        String signatureName = JavaLangUtil.nameOfSignature(methodSignature);
        List<String> signatureParameters = JavaLangUtil.parametersOfSignature(methodSignature);
        Map<String, List<String>> result = new HashMap<>();
        methods.idStream().forEach(methodId -> {
            String mySignature = project.signatureOfMethod(methodId);
            if (!JavaLangUtil.nameOfSignature(mySignature)
                    .equals(signatureName)) {
                return;
            }
            List<String> myParameters = JavaLangUtil.parametersOfSignature(mySignature);
            if (myParameters.size() != signatureParameters.size()) {
                return;
            }
            result.put(methodId, myParameters);
        });

        return result;
    }
}
