package com.pumahawk.dbridge.configuration;

public class GlobalValidatorResource extends BasicResource<GlobalValidatorSpec> {

    @Override
    public Kind getKind() {
        return Kind.GLOBAL_VALIDATOR;
    }
}
