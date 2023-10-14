package org.abego.jareento.javaanalysis;

import org.abego.commons.annotation.SPI;

import java.util.function.Consumer;

@SPI
public interface ProblemsReporter {
    String getID();

    String getTitle();

    void report(Problems problems, Consumer<String> progress);
}
