package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.ProblemReporter;
import org.abego.jareento.javaanalysis.ProblemReporters;

class ProblemReportersImpl
        extends ManyDefault<ProblemReporter>
        implements ProblemReporters {

    private ProblemReportersImpl(Iterable<ProblemReporter> iterable) {
        super(iterable);
    }

    static ProblemReportersImpl newProblemReportersImpl(Iterable<ProblemReporter> iterable) {
        return new ProblemReportersImpl(iterable);
    }
}
