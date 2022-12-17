package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.pumahawk.dbridge.configuration.Validator;
import com.pumahawk.dbridge.exceptions.BadRequestParameterExpeption;
import com.pumahawk.dbridge.script.ScriptManager;
import com.pumahawk.dbridge.script.ScriptManagerFactory;

@SpringBootTest
public class ValidatorManagerTests {

    @Autowired
    private ScriptManagerFactory scriptManagerFactory;

    @Autowired
    private ValidatorManager validatorManager;
    
    public static Stream<Arguments> correctValidationAndConvertion() {
        return Stream.of(
            arguments("notNull", "notNull", null),
            arguments("editValue", "0", 1),
            arguments("editValueAndValidate", "5", 15)
        );
    }
    
    @ParameterizedTest
    @MethodSource
    public void correctValidationAndConvertion(String configurationKey, Object intput, Object expected) throws StreamReadException, DatabindException, IOException {
        ScriptManager context = createContext(intput);
        validatorManager.validate(getParameterConfig(configurationKey), context);
        @SuppressWarnings("unchecked")
        Object input =  ((Map<String, Object>) context.getVariable("ret")).get("input");
        assertEquals(expected, input);
    }

    public static Stream<Arguments> inCorrectValidationAndConvertion() {
        return Stream.of(
            arguments("notNull", null, "Value is null"),
            arguments("validatorNull", null, "Validator is null"),
            arguments("editValueAndValidate", "4", "Value is not equal 10. Value: 9"),
            arguments("validationWithEceptionMessage", "4", "Bad Request")
        );
    }
    
    @ParameterizedTest
    @MethodSource
    public void inCorrectValidationAndConvertion(String configurationKey, Object intput, Object expectedMEssage) throws StreamReadException, DatabindException, IOException {
        BadRequestParameterExpeption ex = assertThrows(BadRequestParameterExpeption.class, () -> validatorManager.validate(getParameterConfig(configurationKey), createContext(intput)));
        assertEquals(expectedMEssage, ex.getResponseMessage());
    }

    @Test
    public void changeParameter() {
        ScriptManager context = scriptManagerFactory.getScriptManager();
        Map<String, Object> params = new HashMap<>();
        context.setVariable("p", params);
        validatorManager.validate(getParameterConfig("changeParameter"), context);
        assertEquals("Mario", params.get("name"));
    }

    private Validator getParameterConfig(String key) {
        try {
            Map<String, Validator> configurations = new YAMLMapper().readValue(getClass().getResource("validators.yaml"), new TypeReference<Map<String, Validator>>() {});
            return configurations.get(key);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ScriptManager createContext(Object input) {
        ScriptManager scriptManager = scriptManagerFactory.getScriptManager();
        scriptManager.setVariable("input", input);
        scriptManager.setVariable("ret", new HashMap<>());
        return scriptManager;
    }

}
