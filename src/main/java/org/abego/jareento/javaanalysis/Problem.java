package org.abego.jareento.javaanalysis;

import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.Properties;

public interface Problem {
    ProblemType getProblemType();

    File getFile();

    int getLineNumber();

    Properties getProperties();

    String getDescription();

    @Nullable
    Object getDetails();
}
