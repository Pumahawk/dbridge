package com.pumahawk.dbridge.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StreamSpelUtils {

    public static Function<Object, Object> fun(Method method) {
        return v -> {
            return inv(method, () -> method.invoke(null, v));
        };
    }

    public static <T> Predicate<T> pred(Method method) {
        return v -> {
            return inv(method, () -> (boolean) method.invoke(null, v));
        };
    }
    
    public static <T> Collector<T, ?, List<T>> toList() {
        return Collectors.toList();
    }

    private static <T> T inv(Method method, SupplierThrowable<T> supp) {
        try {
            return supp.get();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to invoke method. " + String.valueOf(method), e);
        }
    }

    @FunctionalInterface
    private static interface SupplierThrowable<T> {
        public T get() throws Exception;
    }

}
