package com.pumahawk.dbridge.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ParameterUtils {

  @SuppressWarnings("unchecked")
  public static <C, T> List<GroupMap<C, T>> group(List<Map<C, T>> result, C... columns) {
    List<GroupMap<C, T>> list = new LinkedList<GroupMap<C, T>>();
    Map<C, T> lastRow = null;
    List<Map<C, T>> nested = null;
    for (Map<C, T> row : result) {
      if (lastRow != null) {
        if (row != null) {
          boolean parent = true;
          for (C c : columns) {
            Object value = row.get(c);
            Object lastValue = lastRow.get(c);
            if (!(((value == null || lastValue == null) && value != lastValue)
                || !(value != null && value.equals(lastValue)))) {
              parent = false;
              break;
            }
          }
          if (parent) {
            GroupMap<C, T> gm = new GroupMap<>(row);
            gm.getNested().add(row);
            lastRow = row;
            list.add(gm);
            nested = gm.getNested();
          } else {
            if (nested != null) {
              nested.add(row);
            }
          }
        }
      } else {
        lastRow = row;
        GroupMap<C, T> gm = new GroupMap<>(row);
        gm.getNested().add(row);
        list.add(gm);
        nested = gm.getNested();
      }
    }

    return list;
  }

  public static class GroupMap<C, T> extends HashMap<C, T> {

    private List<Map<C, T>> group = new LinkedList<>();

    public GroupMap(Map<? extends C, ? extends T> m) {
      super(m);
    }

    public List<Map<C, T>> getNested() {
      return group;
    }
  }
}
