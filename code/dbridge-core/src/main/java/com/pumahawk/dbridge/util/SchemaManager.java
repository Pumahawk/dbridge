package com.pumahawk.dbridge.util;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pumahawk.dbridge.configuration.Schema;

@Component
public class SchemaManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpelExpressionParser spelExpressionParser;

    @Autowired
    private Supplier<EvaluationContext> evaluationContext;

    public JsonNode process(Schema inputSchema, final Object input) {
        Schema schema = cloneSchema(inputSchema);
        final Object processInput = schema.getInput() != null
            ? expression(schema.getInput(), input)
            : input;
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
        EvaluationContext ec = evaluationContext.get();
        ec.setVariable("input", input);
        return spelExpressionParser.parseExpression(expression).getValue(ec);
    }
    
}
