package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pumahawk.dbridge.util.ParameterUtils.GroupMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ParameterUtilsTests {

  @Test
  public void simpleGroup() {
    List<Map<Integer, String>> results = new LinkedList<>();
    results.add(createRow("1", "Tigre"));
    results.add(createRow("1", "Gatto"));
    results.add(createRow("2", "Cane"));

    List<GroupMap<Integer, String>> groups = ParameterUtils.group(results, 0);
    assertEquals(2, groups.size());
    assertEquals(2, groups.get(0).getNested().size());
    assertEquals(1, groups.get(1).getNested().size());

    assertEquals(groups.get(0).get(0), "1");
    assertEquals(groups.get(0).get(1), "Tigre");
    assertEquals(groups.get(1).get(0), "2");
    assertEquals(groups.get(1).get(1), "Cane");

    assertEquals(groups.get(0).getNested().get(0).get(0), "1");
    assertEquals(groups.get(0).getNested().get(0).get(1), "Tigre");
    assertEquals(groups.get(0).getNested().get(1).get(0), "1");
    assertEquals(groups.get(0).getNested().get(1).get(1), "Gatto");
    assertEquals(groups.get(1).getNested().get(0).get(0), "2");
    assertEquals(groups.get(1).getNested().get(0).get(1), "Cane");
  }

  private Map<Integer, String> createRow(String... data) {
    Map<Integer, String> map = new HashMap<>();
    for (int i = 0; i < data.length; i++) {
      map.put(i, data[i]);
    }
    return map;
  }
}
