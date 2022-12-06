package com.pumahawk.dbridge.services;

import java.util.Map;

public interface QueryParameter {
    String path();
    Map<String, ? extends Object> params();
}
