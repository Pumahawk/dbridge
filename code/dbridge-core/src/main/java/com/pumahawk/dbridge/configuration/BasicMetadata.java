package com.pumahawk.dbridge.configuration;

public class BasicMetadata implements Metadata {

    private String name;

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

}
