package com.pumahawk.dbridge.configuration;

import java.util.List;

public class GlobalValidator {

  private String name;
  private List<Validator> validators;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Validator> getValidators() {
    return validators;
  }

  public void setValidators(List<Validator> validators) {
    this.validators = validators;
  }
}
