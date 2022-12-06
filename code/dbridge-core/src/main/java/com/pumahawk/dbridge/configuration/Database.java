package com.pumahawk.dbridge.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Database {

    private String name;
    private String configurationId;
    private boolean isDefault;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    @JsonProperty("default")
    public boolean isDefault() {
        return isDefault;
    }

    @JsonProperty("default")
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
}
