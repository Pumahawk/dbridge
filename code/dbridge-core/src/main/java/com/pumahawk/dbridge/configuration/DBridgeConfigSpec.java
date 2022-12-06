package com.pumahawk.dbridge.configuration;

import java.util.List;

public class DBridgeConfigSpec implements Spec {

    private List<Database> database;

    public List<Database> getDatabase() {
        return database;
    }

    public void setDatabase(List<Database> database) {
        this.database = database;
    }
}
