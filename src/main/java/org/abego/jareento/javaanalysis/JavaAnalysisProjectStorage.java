package org.abego.jareento.javaanalysis;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Persistently stores {@link JavaAnalysisProject} instances.
 */
public interface JavaAnalysisProjectStorage {

    /**
     * Creates a (persistent) {@link JavaAnalysisProject} for the given
     * {@code projectConfiguration} and returns its {@link URI}.
     * <p>
     * When a JavaAnalysisProject with the same name already exists,
     * updates that project (if required) and returns that project's URI.
     * <p>
     * While creating the JavaAnalysisProject progress information is posted
     * to {@code progress}.
     */
    URI createJavaAnalysisProject(
            JavaAnalysisProjectConfiguration projectConfiguration,
            Consumer<String> progress);

    /**
     * Loads the {@link JavaAnalysisProject} with the given {@link URI} and
     * returns it.
     * <p>
     * While loading progress information is posted to {@code progress}.
     */
    JavaAnalysisProject loadJavaAnalysisProject(
            URI uri, Consumer<String> progress);

    /**
     * Creates a (persistent) {@link JavaAnalysisProject} for the given
     * {@code projectConfiguration} and loads and returns it.
     * <p>
     * When a JavaAnalysisProject with the same name already exists,
     * updates that project (if required) and return that project.
     * <p>
     * While creating the JavaAnalysisProject and loading it progress
     * information is posted to {@code progress}.
     */
    default JavaAnalysisProject createAndLoadJavaAnalysisProject(
            JavaAnalysisProjectConfiguration projectConfiguration,
            Consumer<String> progress) {
        return loadJavaAnalysisProject(
                createJavaAnalysisProject(projectConfiguration, progress),
                progress);
    }
}
