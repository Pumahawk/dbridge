package com.pumahawk.dbridge.script;

public interface ScriptManager {
  void setVariable(String name, Object variable);

  Object getVariable(String name);

  <T> T evaluate(Class<T> type, String expression);

  String evaluateTemplate(String expression);

  default Object evaluate(String expression) {
    return evaluate(Object.class, expression);
  }
}
