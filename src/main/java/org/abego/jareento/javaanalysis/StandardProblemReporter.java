package org.abego.jareento.javaanalysis;

import java.io.File;
import java.io.PrintStream;
import java.util.function.Consumer;

import static org.abego.commons.io.PrintStreamUtil.newPrintStreamToBufferedFile;

public final class StandardProblemReporter implements ProblemReporter {

    private static final String OUTPUT_FILE_NAME = "problems.txt";

    @Override
    public String getID() {
        return "Standard";
    }

    @Override
    public String getTitle() {
        return "Writes problems as tab-separated lines to a file '%s'."
                .formatted(OUTPUT_FILE_NAME);
    }

    @Override
    public void report(
            Problems problems, 
            Consumer<String> progress, 
            ReportParameter reportParameter) {
        
        File reportFile = new File(
                reportParameter.getOutputDirectory(), OUTPUT_FILE_NAME);
        try (PrintStream out = newPrintStreamToBufferedFile(reportFile)) {
            int count = problems.getSize();
            out.println(count + (count == 1 ? " problem." : " problems."));

            problems.stream()
                    .map(StandardProblemReporter::longProblemLineText)
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
