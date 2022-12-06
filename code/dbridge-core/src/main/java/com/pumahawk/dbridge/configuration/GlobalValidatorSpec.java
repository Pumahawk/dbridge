package com.pumahawk.dbridge.configuration;

import java.util.List;

public class GlobalValidatorSpec implements Spec {

    private List<GlobalValidator> globalValidators;

    public List<GlobalValidator> getGlobalValidators() {
        return globalValidators;
    }

    public void setGlobalValidators(List<GlobalValidator> globalValidators) {
        this.globalValidators = globalValidators;
    }
}
