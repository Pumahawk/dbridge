package com.pumahawk.dbridge.services;

import java.util.Map;

import org.springframework.http.HttpMethod;

public interface QueryParameter {
    String path();
    HttpMethod method();
    Map<String, ? extends Object> params();
}
