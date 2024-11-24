package com.pumahawk.dbridge.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"kind"})
public interface ConfigurationResource<T extends Spec> {
  Kind getKind();

  Metadata getMetadata();

  T getSpec();
}
