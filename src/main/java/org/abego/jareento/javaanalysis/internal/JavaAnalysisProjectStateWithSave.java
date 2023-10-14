package org.abego.jareento.javaanalysis.internal;

/**
 * Intentionally "saving" a {@link JavaAnalysisProjectState} is separated from
 * the "core" state feature as the "final" persistence approach is not yet
 * fixed and changing it should not affect code only interested in the State.
 */
public interface JavaAnalysisProjectStateWithSave extends JavaAnalysisProjectState {
    void save();
}
