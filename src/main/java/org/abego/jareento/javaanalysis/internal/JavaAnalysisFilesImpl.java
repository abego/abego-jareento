package org.abego.jareento.javaanalysis.internal;

import java.io.File;

class JavaAnalysisFilesImpl implements JavaAnalysisFiles {
    private final File[] sourceRoots;
    private final File[] dependencies;

    private JavaAnalysisFilesImpl(File[] sourceRoots, File[] dependencies) {
        this.sourceRoots = sourceRoots;
        this.dependencies = dependencies;
    }

    public static JavaAnalysisFilesImpl newJavaAnalysisFilesImpl(
            File[] sourceRoots, File[] dependencies) {
        return new JavaAnalysisFilesImpl(sourceRoots, dependencies);
    }

    @Override
    public File[] getSourceRoots() {
        return sourceRoots;
    }

    @Override
    public File[] getDependencies() {
        return dependencies;
    }
}
