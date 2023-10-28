package org.abego.jareento.javaanalysis;

import java.io.File;
import java.io.PrintStream;
import java.util.function.Consumer;

import static org.abego.commons.io.PrintStreamUtil.newPrintStreamToBufferedFile;

public final class StandardProblemsReporter implements ProblemReporter {

    @Override
    public String getID() {
        return "Standard";
    }

    @Override
    public String getTitle() {
        return "Writes problems as tab-separated lines to a file 'problem.txt'.";
    }

    @Override
    public void report(Problems problems, Consumer<String> progress) {
        File reportFile = new File("problems.txt");
        try (PrintStream out = newPrintStreamToBufferedFile(reportFile)) {
            int count = problems.getSize();
            out.println(count + (count == 1 ? " problem." : " problems."));

            problems.stream()
                    .map(StandardProblemsReporter::longProblemLineText)
                    .forEach(out::println);
        }
        progress.accept(
                "Problem report written to: " + reportFile.getAbsolutePath());
    }

    private static String longProblemLineText(Problem p) {
        return "%s\t%s\t%s\t%s".formatted(
                p.getFile().getAbsolutePath(),
                p.getLineNumber(),
                p.getProblemType().getID(),
                p.getDescription());
    }
}
