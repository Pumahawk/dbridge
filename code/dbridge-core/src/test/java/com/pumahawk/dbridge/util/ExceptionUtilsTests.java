package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.pumahawk.dbridge.exceptions.ProjectException;

public class ExceptionUtilsTests {
    
    public static Stream<Arguments> exceptionUtilsThrow() {
        return Stream.of(
            arguments(ext(() -> ExceptionUtils.throwNotFound(null, "Obj1 not found")), ProjectException.class, "Obj1 not found"),
            arguments(ext(() -> ExceptionUtils.throwNotFound("Obj2 not found")), ProjectException.class, "Obj2 not found"),
            arguments(ext(() -> ExceptionUtils.throwNotFoundFromList(Collections.emptyList(), "Obj3 not found")), ProjectException.class, "Obj3 not found")
        );
    }
    
    @ParameterizedTest
    @MethodSource
    public void exceptionUtilsThrow(Executable execution, Class<?> classEcception, String exceptionMessage) {
        Exception ex = assertThrows(Exception.class, execution);
        String message = ex.getMessage();
        if (ex instanceof ProjectException) {
            message = ((ProjectException) ex).getResponseMessage();
        }
        assertEquals(exceptionMessage, message);
    }
    
    public static Stream<Arguments> exceptionUtilsDoesNotThrow() {
        return Stream.of(
            arguments(sup(() -> ExceptionUtils.throwNotFound(1, "Obj1 not found")), 1),
            arguments(sup(() -> ExceptionUtils.throwNotFoundFromList(Collections.singletonList(2), "Obj3 not found")), 2)
        );
    }
    
    @ParameterizedTest
    @MethodSource
    public void exceptionUtilsDoesNotThrow(ThrowingSupplier<?> execution, Object expected) {
        Object value = assertDoesNotThrow(execution);
        assertEquals(expected, value);
    }

    private static Executable ext(Executable ext) {
        return ext;
    }

    private static ThrowingSupplier<?> sup(ThrowingSupplier<?> sup) {
        return sup;
    }

}
