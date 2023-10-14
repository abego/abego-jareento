package org.abego.jareento.javaanalysis.internal;

import org.abego.commons.util.ListUtil;
import org.abego.jareento.javaanalysis.Problem;
import org.abego.jareento.javaanalysis.Problems;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

class ProblemsImpl implements Problems {
    private final List<Problem> problems;

    private ProblemsImpl(List<Problem> problems) {
        this.problems = problems;
    }

    public static ProblemsImpl newProblemsImpl(Iterable<Problem> problems) {
        return new ProblemsImpl(ListUtil.toList(problems));
    }

    @Override
    public boolean isEmpty() {
        return problems.isEmpty();
    }

    @Override
    public int getSize() {
        return problems.size();
    }

    @Override
    public Stream<Problem> stream() {
        return problems.stream();
    }

    @Override
    public Problems sorted(Comparator<Problem> comparator) {
        return new ProblemsImpl(problems.stream().sorted(comparator).toList());
    }

    @Override
    public Problems sortedByFile() {
        return sorted(ProblemUtil.getProblemByFileComparator());
    }

    @Override
    public Problems sortedByDescription() {
        return sorted(ProblemUtil.getProblemByDescriptionComparator());
    }

    @Override
    public Iterator<Problem> iterator() {
        return problems.iterator();
    }
}
