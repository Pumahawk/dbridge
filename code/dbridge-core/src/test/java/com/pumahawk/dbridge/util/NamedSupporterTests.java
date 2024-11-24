package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NamedSupporterTests {

  @Test
  public void useSingleValue() {
    NamedSupporter<Object> nm = new NamedSupporter<>();
    String key = nm.use("value1");
    Map<String, Object> m = nm.getStore();
    Object element = m.get(editKey(key));
    assertEquals(1, m.size());
    assertEquals("value1", element);
  }

  @Test
  public void useMultipleValues() {
    NamedSupporter<Object> nm = new NamedSupporter<>();
    String k1 = nm.use("A");
    String k2 = nm.use("B");
    String k3 = nm.use(null);

    Map<String, Object> m = nm.getStore();

    assertEquals(3, m.size());
    assertEquals("A", m.get(editKey(k1)));
    assertEquals("B", m.get(editKey(k2)));
    assertEquals(null, m.get(editKey(k3)));
  }

  @Test
  public void existingMaps() {
    Map<String, Object> map = new HashMap<>();
    map.put("v1", "value1");
    map.put("v2", "value2");
    NamedSupporter<Object> nm = new NamedSupporter<>(map.entrySet());
    String k1 = nm.use("A");

    Map<String, Object> m = nm.getStore();

    assertEquals(3, m.size());
    assertEquals("A", m.get(editKey(k1)));
    assertEquals("value1", m.get("v1"));
    assertEquals("value2", m.get("v2"));
  }

  @Test
  public void emptyList() {
    NamedSupporter<Object> nm = new NamedSupporter<>(Collections.emptyList());
    String k1 = nm.use("A");

    Map<String, Object> m = nm.getStore();

    assertEquals("A", m.get(editKey(k1)));
  }

  @Test
  public void useIterator() {
    List<Object> values = new LinkedList<>();
    NamedSupporter<Object> nm = new NamedSupporter<>(Collections.emptyList());
    nm.use("A");

    nm.forEach(v -> values.add(v.getValue()));

    assertEquals(1, values.size());
    assertEquals("A", values.get(0));
  }

  private Object editKey(String k3) {
    return k3.replaceFirst("^:", "");
  }
}
