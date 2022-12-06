package com.pumahawk.dbridge.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Validator {

    private String name;
    private String extendz;
    private String input;
    private String convert;
    private ValidatorRule validator;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("_extends")
    public String getExtends() {
        return extendz;
    }

    @JsonProperty("_extends")
    public void setExtends(String extendz) {
        this.extendz = extendz;
    }

    @JsonProperty("_input")
    public String getInput() {
        return input;
    }

    @JsonProperty("_input")
    public void setInput(String input) {
        this.input = input;
    }

    public String getConvert() {
        return convert;
    }

    public void setConvert(String convert) {
        this.convert = convert;
    }

    public ValidatorRule getValidator() {
        return validator;
    }

    public void setValidator(ValidatorRule validator) {
        this.validator = validator;
    }
}
