package com.pumahawk.dbridge.configuration;

public enum Kind {
  GLOBAL_VALIDATOR("global-validator", GlobalValidatorResource.class),
  DBRIDGE_CONFIG("dbridge-config", DBridgeConfigResource.class),
  QUERY("query", QueryResource.class),
  ;

  private final String name;
  private final Class<? extends ConfigurationResource<?>> type;

  Kind(String name, Class<? extends ConfigurationResource<?>> type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Class<? extends ConfigurationResource<?>> getType() {
    return type;
  }

  public static Kind fromName(String name) {
    for (Kind k : Kind.values()) {
      if (k.getName().equals(name)) {
        return k;
      }
    }
    throw new IllegalArgumentException("unable to find Kind from name. Name: " + name);
  }
}
