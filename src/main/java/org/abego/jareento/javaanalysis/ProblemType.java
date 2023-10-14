package org.abego.jareento.javaanalysis;

public interface ProblemType {
    String getID();

    String getTitle();

    String getDetails();

    default String getDescriptionTemplate() {
        return getTitle();
    }
}
