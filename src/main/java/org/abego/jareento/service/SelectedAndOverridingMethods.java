package org.abego.jareento.service;

import org.abego.jareento.javaanalysis.JavaMethodDeclarators;

public interface SelectedAndOverridingMethods {
    JavaMethodDeclarators selectedMethods();

    JavaMethodDeclarators overridingMethods();
}
