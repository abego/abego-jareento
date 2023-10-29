package org.abego.jareento.javaanalysis.internal;

import org.abego.jareento.base.Many;

import java.util.Set;

public interface IDs extends Many<String> {
    Set<String> toSet();
}
