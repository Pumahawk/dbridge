package com.pumahawk.dbridge.configuration;

public class Query {

    private String database;
    private String sql;

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
}
