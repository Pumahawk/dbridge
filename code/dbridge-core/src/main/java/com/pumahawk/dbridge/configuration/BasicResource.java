package com.pumahawk.dbridge.configuration;

public abstract class BasicResource<T extends Spec> implements ConfigurationResource<T> {

  private BasicMetadata metadata;
  private T spec;

  @Override
  public Metadata getMetadata() {
    return metadata;
  }

  @Override
  public T getSpec() {
    return spec;
  }

  public void setMetadata(BasicMetadata metadata) {
    this.metadata = metadata;
  }

  public void setSpec(T spec) {
    this.spec = spec;
  }
}
