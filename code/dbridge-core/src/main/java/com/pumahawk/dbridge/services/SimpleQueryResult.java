package com.pumahawk.dbridge.services;

import com.fasterxml.jackson.databind.JsonNode;

public class SimpleQueryResult implements QueryResult {

    private JsonNode data;

    public void setData(JsonNode data) {
        this.data = data;
    }

    public JsonNode getData() {
        return data;
    }
    
}
