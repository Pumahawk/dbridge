package com.pumahawk.dbridge.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@SpringBootTest
public class DBridgeConfigResourceTests {
    
    @Autowired
    private YAMLMapper yamlMapper;

    @Test
    public void completeMapping() throws StreamReadException, DatabindException, IOException {

        DBridgeConfigResource dbridgeConfigResource = yamlMapper.readValue(getConfigFile("dbridge-config-complete.yaml"), DBridgeConfigResource.class);
        assertEquals(Kind.DBRIDGE_CONFIG, dbridgeConfigResource.getKind());
        assertEquals("demo-configuration", dbridgeConfigResource.getMetadata().getName());

        DBridgeConfigSpec spec = dbridgeConfigResource.getSpec();
        assertEquals(2, spec.getDatabase().size());

        List<Database> dbs = spec.getDatabase();

        assertEquals("h2", dbs.get(0).getName());
        assertEquals("h2-id", dbs.get(0).getConfigurationId());
        assertEquals(true, dbs.get(0).isDefault());

        assertEquals("postgres", dbs.get(1).getName());
        assertEquals("postgres-id", dbs.get(1).getConfigurationId());
        assertEquals(false, dbs.get(1).isDefault());

    }

    private File getConfigFile(String file) {
        return new File(getClass().getResource("dbridge-config/" + file).getPath());
    }

}
