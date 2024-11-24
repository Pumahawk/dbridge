package com.pumahawk.dbridge.util;

import com.pumahawk.dbridge.exceptions.ProjectException;
import java.util.List;
import org.springframework.http.HttpStatus;

public class ExceptionUtils {
  public static <T> T throwNotFound(T value, String message) {
    if (value == null) {
      throw new ProjectException(HttpStatus.NOT_FOUND, message);
    }
    return value;
  }

  public static <T> T throwNotFound(String message) {
    return throwNotFound(null, message);
  }

  public static <T> T throwNotFoundFromList(List<T> values, String message) {
    if (values == null || values.size() == 0) {
      return throwNotFound(message);
    } else {
      return values.get(0);
    }
  }
}
