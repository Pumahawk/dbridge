package com.pumahawk.dbridge.configuration;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Schema {

  private final boolean schema;
  private String input;

  @JsonIgnore private final String value;

  @JsonIgnore private Map<String, Schema> fields = new HashMap<String, Schema>();

  public Schema(String value) {
    this.value = value;
    this.schema = value == null;
  }

  public Schema() {
    this(null);
  }

  @JsonProperty("_input")
  public String getInput() {
    return input;
  }

  @JsonProperty("_input")
  public void setInput(String input) {
    this.input = input;
  }

  public Map<String, Schema> getFields() {
    return fields;
  }

  public void setFields(Map<String, Schema> fields) {
    this.fields = fields;
  }

  public String getValue() {
    return value;
  }

  public boolean isSchema() {
    return schema;
  }

  @JsonAnySetter
  public void addField(String field, JsonNode node) {
    Optional.ofNullable(node)
        .map(
            n ->
                n.isTextual()
                    ? new Schema(node.asText())
                    : new ObjectMapper().convertValue(node, Schema.class))
        .ifPresent(s -> fields.put(field, s));
  }
}
