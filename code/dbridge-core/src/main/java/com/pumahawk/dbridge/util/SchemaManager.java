package com.pumahawk.dbridge.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pumahawk.dbridge.configuration.Schema;
import com.pumahawk.dbridge.script.ScriptManager;
import com.pumahawk.dbridge.script.ScriptManagerFactory;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SchemaManager {

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ScriptManagerFactory scriptManagerFactory;

  public JsonNode process(Schema inputSchema, final Object input) {
    Schema schema = cloneSchema(inputSchema);
    final Object processInput =
        schema.getInput() != null ? expression(schema.getInput(), input) : input;
    schema.setInput(null);
    if (processInput != null) {
      if (processInput instanceof Iterable) {
        return processIterable(schema, (Iterable<?>) processInput);
      } else if (schema.isSchema()) {
        return processObject(schema.getFields(), processInput);
      } else {
        return processLeaf(schema.getValue(), processInput);
      }
    } else {
      return null;
    }
  }

  private JsonNode processIterable(Schema schema, Iterable<?> processInput) {
    ArrayNode ar = objectMapper.createArrayNode();
    for (Object v : (Iterable<?>) processInput) {
      ar.add(process(schema, v));
    }
    return ar;
  }

  private JsonNode processObject(Map<String, Schema> fields, Object processInput) {
    ObjectNode on = objectMapper.createObjectNode();
    fields.forEach((f, s) -> on.set(f, process(s, processInput)));
    return on;
  }

  private JsonNode processLeaf(String expression, Object processInput) {
    return objectMapper.convertValue(expression(expression, processInput), JsonNode.class);
  }

  private Schema cloneSchema(Schema inputSchema) {
    Schema sc = new Schema(inputSchema.getValue());
    sc.setInput(inputSchema.getInput());
    sc.setFields(inputSchema.getFields());
    return sc;
  }

  private Object expression(String expression, Object input) {
    ScriptManager scriptManager = scriptManagerFactory.getScriptManager();
    scriptManager.setVariable("input", input);
    return scriptManager.evaluate(expression);
  }
}
