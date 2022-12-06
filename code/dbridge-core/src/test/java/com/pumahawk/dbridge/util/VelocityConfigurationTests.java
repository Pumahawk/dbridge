package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        "velocity.custom-property=hello-value"
    },
    classes = {
        VelocityConfiguration.class,
    })
public class VelocityConfigurationTests {

    @Autowired
    public VelocityEngine velocityEngine;

    @Test
    public void loadContext(){
    }

    @Test
    public void setProperties() {
        assertEquals("hello-value", velocityEngine.getProperty("custom-property"));
    }
}
