package com.pumahawk.dbridge.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Query {

    private String name;
    private String database;
    private String sql;
    @JsonProperty("_input")
    private String _input;
    private String conversion;
    private boolean update = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public String getConversion() {
        return conversion;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion;
    }

    @JsonProperty("_input")
    public String getInput() {
        return _input;
    }

    @JsonProperty("_input")
    public void setInput(String _input) {
        this._input = _input;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isUpdate() {
        return update;
    }
}
