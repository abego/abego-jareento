package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.base.ID;

/**
 * Utility class to access information embedded in certain IDs.
 * <p>
 * To avoid some database lookups IDs for certain domain classes are opaque only
 * to the client code but internally hold some meaning. This utility class knows
 * about the inner structure of those IDs and gives access to it.
 */
public class IDSyntaxUtil {

    //region @ID("MethodCall") - `{callKind}-{callingMethodId}@{locationInCallingMethod}`
    // - callKind: the bytecode mnemonic of the call ("invoke...", e.g. "invokevirtual")
    // - callingMethodId: @ID("JavaMethod") of method containing the method call
    // - locationInCallingMethod: the bytecode offset of the invoke call within 
    //   the calling method, used to differentiate multiple calls in the same method

    /**
     * Returns a new methodCallId based on the given input.
     */
    public static String newMethodCallId(
            String callKind, String callingMethodId, String locationInCallingMethod) {
        return callKind + "-" + callingMethodId + "@" + locationInCallingMethod;
    }

    /**
     * Returns the parts of the {@code methodCallId}: [callKind, callingMethodId, locationInCallingMethod].
     */
    public static String idOfCallingMethodOfMethodCall(@ID("MethodCall") String methodCallId) {
        int i = methodCallId.indexOf('-');
        int j = methodCallId.indexOf('@');
        if (i < 0 || j <= i) {
            throw new IllegalArgumentException("Invalid method call id: " + methodCallId);
        }
        return methodCallId.substring(i + 1, j);
    }

    //endregion
}
