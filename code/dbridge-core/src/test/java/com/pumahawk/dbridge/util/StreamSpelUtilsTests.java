package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;

public class StreamSpelUtilsTests {

    @Test
    public void createInstance() {
        assertDoesNotThrow(() -> new StreamSpelUtils());
    }

    @Test
    public void mapStream() throws NoSuchMethodException, SecurityException {
        Function<Object, Object> obj = StreamSpelUtils.fun(NumberUtils.class.getMethod("toInt", String.class));
        int value = (int) obj.apply("1234");
        assertEquals(1234, value);
    }

    @Test
    public void mapThrowIllegalArgument() throws NoSuchMethodException, SecurityException {
        Function<Object, Object> obj = StreamSpelUtils.fun(StringUtils.class.getMethod("difference", String.class, String.class));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> obj.apply("1234"));
        assertTrue(ex.getMessage().contains("difference"));
    }
    
    @Test
    public void toList() {
        List<String> values = Stream.of("value1").collect(StreamSpelUtils.toList());
        assertEquals(1, values.size());
        assertEquals("value1", values.get(0));
    }

    @Test
    public void predStream() throws NoSuchMethodException, SecurityException {
        List<String> values = Stream.of("a", "4", "10", "b").filter(StreamSpelUtils.pred(NumberUtils.class.getMethod("isParsable", String.class))).collect(Collectors.toList());
        assertEquals(2, values.size());
        assertEquals("4", values.get(0));
        assertEquals("10", values.get(1));
    }


}
