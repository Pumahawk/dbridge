package com.pumahawk.dbridge.endtoend;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.pumahawk.dbridge.DBridgeCoreApplication;

@EndToEndTest
public class BlogPostgresEndToEndTests extends BaseTestConteinerEndToEndTest {

    @Autowired
    private WebTestClient client;

    @Test
    public void loadContext() {
    }

    public String getResource(String name) {
        try {
            return IOUtils.toString(getClass().getResourceAsStream("/endtoend/" + name), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Unable to open content file " + name, e);
        }
    }

    public WebTestClient client() {
        return client;
    }

    @Configuration
    @PropertySource("classpath:/endtoend/endtoend.properties")
    @EnableAutoConfiguration
    @Import(DBridgeCoreApplication.class)
    public static class Conf {

    }
    
}
