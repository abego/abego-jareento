package org.abego.jareento.javaanalysis;

import com.github.javaparser.ast.CompilationUnit;
import org.abego.commons.annotation.SPI;

import java.util.function.Consumer;

@SPI
public interface ProblemChecker {
    /**
     * Returns the type of problem this checker can detect.
     */
    ProblemType getProblemType();

    /**
     * Checks the given {@code compilationUnit} for {@link Problem}s and passed
     * any detected {@code Problem} to the {@code problemConsumer}.
     * <p>
     * The same {@link ProblemChecker} instance may be used to check multiple
     * {@link CompilationUnit}s.
     */
    void checkForProblems(
            CompilationUnit compilationUnit, Consumer<Problem> problemConsumer);

    /**
     * Called at the start of a problem check session, before any
     * compilationUnit is checked for problems.
     * <p>
     * Typically, implementations will initialize some shared state, to be used
     * across multiple invocations of {@link #checkForProblems(CompilationUnit, Consumer)}.
     */
    default void beginCheck() {
    }

    /**
     * Called at the end of a problem check session, after all
     * compilationUnits are checked for problems.
     * <p>
     * Typically, implementations will analyze some shared state, holding
     * information assembled during multiple invocations of
     * {@link #checkForProblems(CompilationUnit, Consumer)} and possibly detect
     * new {@link Problem}s that are then passed to the {@code problemConsumer}.
     */
    default void endCheck(Consumer<Problem> problemConsumer) {
    }
}
