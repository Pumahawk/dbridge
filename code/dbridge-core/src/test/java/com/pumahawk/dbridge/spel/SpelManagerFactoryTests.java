package com.pumahawk.dbridge.spel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collections;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.pumahawk.dbridge.script.ScriptManager;

@SpringBootTest
public class SpelManagerFactoryTests {

    @Autowired
    private SpelManagerFactory spelManagerFactory;

    @Test
    public void callCustomFunction() {
        Boolean value = spelManagerFactory.getScriptManager().evaluate(Boolean.class, "#isEmpty('')");
        assertNotNull(value);
        assertTrue(value.booleanValue());
    }

    public static Stream<Arguments> functionsConfiguration() {
        return Stream.of(
            arguments("#isNumber(#input)", "1", true),
            arguments("#isEmpty(#input)", "", true),
            arguments("#trim(#input)", "   the message   ", "the message"),
            arguments("#toNumber(#input)", "1", 1),
            arguments("#is(#input).equalTo(1)", 1, true),
            arguments("#fgt(2).and(#flt(8)).test(#input)", 5, true),
            arguments("#input gt 2 and #input lt 8", 5, true),
            arguments("#group({{id:1, name:'a'},{id:1,name:'B'}}, 'id')[0].nested[1]['name']", null, "B"),
            arguments("#found(#input, 'check not found')", "exist", "exist"),
            arguments("#foundFirst(#input, 'check not found')", Collections.singleton("exist"), "exist"),
            arguments("#input.stream().map(#fun(#toNumber)).collect(#toList()).get(0)", Collections.singleton("1"), 1),
            arguments("#input.filter(#pred(#isNumber)).map(#fun(#toNumber)).collect(#toList()).get(0)", Stream.of("a","10","b"), 10)
        );
    }
    
    @ParameterizedTest
    @MethodSource
    public void functionsConfiguration(String expression, Object input, Object expected) {;
        ScriptManager spelManager = spelManagerFactory.getScriptManager();
        spelManager.setVariable("input", input);
        Object out = spelManager.evaluate(expression);
        assertEquals(expected, out);
    }

    @Test
    public void callCustomBean() {
        String value = spelManagerFactory.getScriptManager().evaluate(String.class, "@spelTestBean.setPrefix('Hello, ', 'World')");
        assertNotNull(value);
        assertEquals("Hello, World", value);
    }

    @Test
    public void throwError() {
        Exception e = assertThrows(Exception.class, () -> spelManagerFactory.getScriptManager().evaluate("#found(null, 'not found test parameter')"));
        ResponseStatusException ex = ExceptionUtils.throwableOfType(e, ResponseStatusException.class);
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("not found test parameter", ex.getReason());
        
    }

    @Test
    public void getVariable() {
        ScriptManager sm = spelManagerFactory.getScriptManager();
        sm.setVariable("custom", "Hello, World!");
        assertEquals("Hello, World!", sm.getVariable("custom"));
    }

}
