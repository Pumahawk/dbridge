package com.pumahawk.dbridge.spel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
public class SpelConfigurationTests {

    @Autowired
    private SpelExpressionParser spelExpressionParser;

    @Autowired
    private Supplier<EvaluationContext> evaliationContext;

    @Test
    public void callCustomFunction() {
        Boolean value = spelExpressionParser.parseExpression("#isEmpty('')").getValue(context(), Boolean.class);
        assertNotNull(value);
        assertTrue(value.booleanValue());
    }

    public static Stream<Arguments> functionsConfiguration() {
        return Stream.of(
            arguments("#isNumber(#this)", "1", true),
            arguments("#isEmpty(#this)", "", true),
            arguments("#trim(#this)", "   the message   ", "the message"),
            arguments("#toNumber(#this)", "1", 1),
            arguments("#is(#this).equalTo(1)", 1, true),
            arguments("#fgt(2).and(#flt(8)).test(#this)", 5, true),
            arguments("#this gt 2 and #this lt 8", 5, true),
            arguments("#group({{id:1, name:'a'},{id:1,name:'B'}}, 'id')[0].nested[1]['name']", null, "B"),
            arguments("#found(#this, 'check not found')", "exist", "exist"),
            arguments("#foundFirst(#this, 'check not found')", Collections.singleton("exist"), "exist")
        );
    }
    
    @ParameterizedTest
    @MethodSource
    public void functionsConfiguration(String expression, Object input, Object expected) {;
        Object out = spelExpressionParser.parseExpression(expression).getValue(context(), input);
        assertEquals(expected, out);
    }

    @Test
    public void callCustomBean() {
        String value = spelExpressionParser.parseExpression("@spelTestBean.setPrefix('Hello, ', 'World')").getValue(context(), String.class);
        assertNotNull(value);
        assertEquals("Hello, World", value);
    }

    @Test
    public void throwError() {
        Exception e = assertThrows(Exception.class, () -> spelExpressionParser.parseExpression("#found(null, 'not found test parameter')").getValue(context()));
        ResponseStatusException ex = ExceptionUtils.throwableOfType(e, ResponseStatusException.class);
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("not found test parameter", ex.getReason());
        
    }

    private EvaluationContext context() {
        return evaliationContext.get();
    }
}
