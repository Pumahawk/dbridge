package com.pumahawk.dbridge.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class KindTests {

  @Test
  public void convertAll() {
    for (Kind kind : Kind.values()) {
      assertEquals(kind, Kind.fromName(kind.getName()));
    }
  }

  @Test
  public void notFound() {
    String name = "not-found";
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> Kind.fromName(name));
    assertTrue(e.getMessage().contains(name));
  }
}
