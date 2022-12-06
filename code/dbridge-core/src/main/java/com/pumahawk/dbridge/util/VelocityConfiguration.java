package com.pumahawk.dbridge.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class VelocityConfiguration {
    
    @Bean
    public VelocityEngine velocityEngine(Map<String, String> velocityProperties) {
        Properties pr = new Properties();
        VelocityEngine engine = new VelocityEngine();
        velocityProperties.forEach(pr::put);
        engine.init(pr);
        return engine;
    }

    @Bean
    @ConfigurationProperties("velocity")
    public Map<String, String> velocityProperties() {
        return new HashMap<>();
    }
}
