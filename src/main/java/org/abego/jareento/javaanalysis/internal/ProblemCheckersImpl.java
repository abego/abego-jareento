package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.javaanalysis.ProblemChecker;
import org.abego.jareento.javaanalysis.ProblemCheckers;

class ProblemCheckersImpl
        extends ManyDefault<ProblemChecker>
        implements ProblemCheckers {

    private ProblemCheckersImpl(Iterable<ProblemChecker> iterable) {
        super(iterable);
    }

    static ProblemCheckersImpl newProblemCheckersImpl(Iterable<ProblemChecker> iterable) {
        return new ProblemCheckersImpl(iterable);
    }
}
