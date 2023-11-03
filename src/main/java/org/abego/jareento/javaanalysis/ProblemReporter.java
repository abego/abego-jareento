package org.abego.jareento.javaanalysis;

import org.abego.commons.annotation.SPI;

import java.io.File;
import java.util.function.Consumer;

@SPI
public interface ProblemReporter {
    interface ReportParameter {
        File getOutputDirectory();
    }
    
    String getID();

    String getTitle();

    void report(Problems problems, 
                Consumer<String> progress, 
                ReportParameter reportParameter);
}
