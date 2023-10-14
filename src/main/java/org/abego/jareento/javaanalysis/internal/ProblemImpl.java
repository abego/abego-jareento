package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.ProblemType;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.Properties;

import static org.abego.commons.stringevaluator.StringEvaluatorUtil.evaluatedString;

class ProblemImpl implements Problem {
    private final ProblemType problemType;
    private final long locationInFileId;
    private final Properties properties;
    @Nullable
    private final Object details;

    private ProblemImpl(
            ProblemType problemType,
            long locationInFileId,
            Properties properties,
            @Nullable Object details) {

        this.problemType = problemType;
        this.properties = properties;
        this.locationInFileId = locationInFileId;
        this.details = details;
    }

    public static ProblemImpl newProblemImpl(
            ProblemType problemType,
            long locationInFileId,
            Properties properties,
            @Nullable Object details) {

        return new ProblemImpl(problemType, locationInFileId, properties, details);
    }

    @Override
    public ProblemType getProblemType() {
        return problemType;
    }

    @Override
    public File getFile() {
        return LocationInFile.getFile(locationInFileId);
    }

    @Override
    public int getLineNumber() {
        return LocationInFile.getLineNumber(locationInFileId);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getDescription() {
        return evaluatedString(
                getProblemType().getDescriptionTemplate(), getProperties());
    }

    @Nullable
    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProblemImpl myProblem = (ProblemImpl) o;
        return locationInFileId == myProblem.locationInFileId
                && problemType.equals(myProblem.problemType)
                && properties.equals(myProblem.properties)
                && Objects.equals(details, myProblem.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(problemType, locationInFileId, properties, details);
    }
}
