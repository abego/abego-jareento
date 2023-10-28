package org.abego.jareento.javaanalysis;

import org.abego.jareento.base.ManyWithId;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface JavaMethods extends ManyWithId<JavaMethod, JavaMethods> {
    Stream<String> idStream();

    /**
     * Returns all methods that have the same name as the name of the
     * {@code methodSignature} and the same number of parameters, as a map with
     * the method's id as key and the method's parameter types as value.
     * <p>
     * Note: the result may also include methods that don't have parameter type
     * compatible with the types of the methodSignature. Finding the "proper"
     * method out of these candidates must be done in a separate step.
     */
    Map<String, List<String>> methodCandidatesForSignature(String methodSignature);
}
