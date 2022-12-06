package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import com.fasterxml.jackson.databind.JsonNode;
import com.pumahawk.dbridge.configuration.ConfigurationException;
import com.pumahawk.dbridge.util.DBridgeConstants.Properties;

@SpringBootTest
public class ConfigurationLoaderTests {

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private ApplicationContext ac;
    
    @Test
    public void loadContext() {
    }

    @Test
    public void loadAllConfiguration() {
        List<JsonNode> configs = configurationLoader.getAllConfigurations();
        List<String> names = configs.stream()
            .map(n -> n.at("/metadata/name").asText())
            .filter(n -> n != null)
            .collect(Collectors.toList());
        List<String> expected = Arrays.asList(
            "demo-configuration",
            "articles-by-id",
            "articles",
            "user-articles",
            "user-by-id",
            "users",
            "basic-id-validators",
            "basic-search-validators"
        );
        assertTrue(names.containsAll(expected));
    }

    @Test
    public void configurationStore() {
        assertDoesNotThrow(() ->  configurationLoader.getConfigurationStore());
    }

    @Test
    public void resourceDoesNotExist() {
        Exception e = assertThrows(Exception.class, () -> getCustomConfigurationLoader(env -> env
            .setProperty(Properties.CONFIGURATION_PATH, "classpath:notfound")));
        ConfigurationException ex = ExceptionUtils.throwableOfType(e, ConfigurationException.class);
        assertTrue(ex.getMessage().contains("notfound"));
    }

    @Test
    public void resourceIsInvalid() {
        Exception e = assertThrows(Exception.class, () -> getCustomConfigurationLoader(env -> env
            .setProperty(Properties.CONFIGURATION_PATH, "file:" + getClass().getResource("isfile").getPath())));
        ConfigurationException ex = ExceptionUtils.throwableOfType(e, ConfigurationException.class);
        assertTrue(ex.getMessage().contains("Unable get retrieve configuration directory"));
    }

    @Test
    public void resourceIsInvalidTypeOfKind() {
        ConfigurationLoader rl = getCustomConfigurationLoader(env -> env.setProperty(Properties.CONFIGURATION_PATH, "file:" + getClass().getResource("invalid-kind").getPath()));
        ConfigurationException e = assertThrows(ConfigurationException.class, () -> rl.getConfigurationStore());
        assertTrue(e.getMessage().contains("Unable to retrieve kind"));
    }

    @Test
    public void resourceWithYamlError() {
        ConfigurationLoader rl = getCustomConfigurationLoader(env -> env.setProperty(Properties.CONFIGURATION_PATH, "file:" + getClass().getResource("yaml-with-error").getPath()));
        ConfigurationException e = assertThrows(ConfigurationException.class, () -> rl.getConfigurationStore());
        assertTrue(e.getMessage().contains("with-error.dbridge.yaml"));
    }

    @Test
    public void ignoreFileToLoad() {
        ConfigurationLoader rl = getCustomConfigurationLoader(env -> env.setProperty(Properties.CONFIGURATION_PATH, "file:" + getClass().getResource("no-resources").getPath()));
        ConfigurationStore store = rl.getConfigurationStore();
        assertEquals(0, store.size());
    }

    public ConfigurationLoader getCustomConfigurationLoader(Consumer<MockEnvironment> setEnv) {
        try(AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext()) {
            ac.setParent(this.ac);
            ac.register(ConfigurationLoader.class);
            MockEnvironment env = new MockEnvironment();
            setEnv.accept(env);
            ac.setEnvironment(env);
            ac.refresh();
            return ac.getBean(ConfigurationLoader.class);
        }
    }
}
