package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.pumahawk.dbridge.configuration.Schema;

@SpringBootTest
public class SchemaManagerTests {

    @Autowired
    private SchemaManager schemaManager;

    @Test
    public void simple() {
        Schema schema = loadConfig("simple");
        JsonNode result = schemaManager.process(schema, null);
        assertEquals("Hello, World!", result.get("name").asText());
    }

    @Test
    public void withInput() {
        Schema schema = loadConfig("withInput");

        List<User> input = new LinkedList<>();
        input.add(new User("Mario", "Rossi"));
        input.add(new User("Luigi", "Verdi"));
        
        JsonNode result = schemaManager.process(schema, input);

        assertEquals("Luigi", result.get("name").asText());
        assertEquals("Verdi", result.get("surname").asText());
    }

    @Test
    public void withInputDoubleCall() {
        Schema schema = loadConfig("withInput");

        List<User> input = new LinkedList<>();
        input.add(new User("Mario", "Rossi"));
        input.add(new User("Luigi", "Verdi"));
        
        schemaManager.process(schema, input);
        JsonNode result = schemaManager.process(schema, input);

        assertEquals("Luigi", result.get("name").asText());
        assertEquals("Verdi", result.get("surname").asText());
    }

    @Test
    public void withInputArray() {
        Schema schema = loadConfig("withInputArray");

        List<User> input = new LinkedList<>();
        input.add(new User("Mario", "Rossi"));
        input.add(new User("Luigi", "Verdi"));
        
        JsonNode results = schemaManager.process(schema, input);
        JsonNode result;

        result = results.get(0);
        assertEquals("Mario", result.get("name").asText());
        assertEquals("Rossi", result.get("surname").asText());

        result = results.get(1);
        assertEquals("Luigi", result.get("name").asText());
        assertEquals("Verdi", result.get("surname").asText());
    }

    @Test
    public void inlineMap() {
        Schema schema = loadConfig("inlineMap");
        JsonNode map = schemaManager.process(schema, null);
        assertEquals("n1", map.get("name").asText());
        assertEquals("s1", map.get("surname").asText());
    }
    

    @Test
    public void inlineList() {
        Schema schema = loadConfig("inlineList");
        JsonNode list = schemaManager.process(schema, null);
        assertEquals(2, list.size());
        assertEquals("1", list.get(0).get("value").asText());
        assertEquals("2", list.get(1).get("value").asText());
    }

    @Test
    public void inlineListWithMap() {
        Schema schema = loadConfig("inlineListWithMap");
        JsonNode list = schemaManager.process(schema, null);
        assertEquals(1, list.size());
        JsonNode map = list.get(0);
        assertEquals("n1", map.get("name").asText());
        assertEquals("s1", map.get("surname").asText());
    }

    @Test
    public void withNullInput() {
        Schema schema = loadConfig("withNullInput");

        List<User> input = null;
        
        Object result = schemaManager.process(schema, input);

        assertNull(result);
    }

    @Test
    public void withNullInputInSpel() {
        Schema schema = loadConfig("withNullInputInSpel");

        List<User> input = Collections.emptyList();
        
        Object result = schemaManager.process(schema, input);

        assertNull(result);
    }
    


    private Schema loadConfig(String name) {
        try {
            return new YAMLMapper().readValue(getClass().getResourceAsStream("schema.yaml"), new TypeReference<Map<String, Schema>>(){}).get(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class User {

        private final String name;
        private final String surname;
        
        public User(String name, String surname) {
            this.name = name;
            this.surname = surname;
        }

        public String getName() {
            return name;
        }

        public String getSurname() {
            return surname;
        }
    }
}
