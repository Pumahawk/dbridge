package com.pumahawk.dbridge.endtoend;

import java.util.function.Function;

public class CustomUtils {
  public static Function<String, String> replaceAll(String regex, String value) {
    return s -> s != null ? s.replaceAll(regex, value) : null;
  }
}
