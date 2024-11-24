package com.pumahawk.dbridge.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class NamedSupporter<T> implements Iterable<Entry<String, T>> {

  private final Map<String, T> store;

  private int counter = 0;

  public NamedSupporter(Iterable<Entry<String, T>> map) {
    this.store = createMapStore(map);
  }

  public NamedSupporter() {
    this(Collections.emptyList());
  }

  public String use(T element) {
    String key = "NamedSupporter" + counter++;
    store.put(key, element);
    return ":" + key;
  }

  private Map<String, T> createMapStore(Iterable<Entry<String, T>> map) {
    Map<String, T> store = new HashMap<>();
    for (Entry<String, T> el : map) {
      store.put(el.getKey(), el.getValue());
    }
    return store;
  }

  @Override
  public Iterator<Entry<String, T>> iterator() {
    return store.entrySet().iterator();
  }

  public Map<String, T> getStore() {
    return store;
  }
}
