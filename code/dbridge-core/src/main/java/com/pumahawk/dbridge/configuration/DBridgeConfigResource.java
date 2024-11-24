package com.pumahawk.dbridge.configuration;

public class DBridgeConfigResource extends BasicResource<DBridgeConfigSpec> {

  @Override
  public Kind getKind() {
    return Kind.DBRIDGE_CONFIG;
  }
}
