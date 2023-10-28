package org.abego.jareento.service;

import org.abego.jareento.base.JavaMethodDeclaratorSet;

public interface SelectedAndOverridingMethods {
    JavaMethodDeclaratorSet selectedMethods();

    JavaMethodDeclaratorSet overridingMethods();
}
