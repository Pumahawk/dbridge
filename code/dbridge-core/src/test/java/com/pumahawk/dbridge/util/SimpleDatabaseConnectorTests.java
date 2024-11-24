package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pumahawk.dbridge.configuration.Database;

@SpringBootTest(properties = {
    "config.jdbcUrl=jdbc:h2:mem:",
    "config.username=sa",
    "config.driverClassName=org.h2.Driver",
})
public class SimpleDatabaseConnectorTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SimpleDatabaseConnector simpleDatabaseConnector;

    @Test
    public void createConnectionH2() {
        DataSource ds = simpleDatabaseConnector.configure("config");
        Connection con = assertDoesNotThrow(() -> ds.getConnection());
        assertDoesNotThrow(() -> con
            .createStatement()
            .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))"));
        assertDoesNotThrow(() -> con.close());
    }

    @Test
    public void getConnectionById() {
    
        Database db1 = new Database();
        db1.setConfigurationId("config");
        db1.setName("unit-test-config1");

        Database db2 = new Database();
        db2.setConfigurationId("config");
        db2.setName("unit-test-config2");

        AnnotationConfigApplicationContext anc = new AnnotationConfigApplicationContext();
        SimpleDatabaseConnector dbc = initWithDatabaseStream(anc, db1, db2);
        Optional<DataSource> ds = dbc.getById("unit-test-config1");
        assertTrue(ds.isPresent());
        assertFalse(dbc.getDefault().isPresent());
        assertFalse(dbc.getById("not-exist-test").isPresent());

        assertDoesNotThrow(() -> ds.get().getConnection()
            .createStatement()
            .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))"));

        assertDoesNotThrow(() -> dbc.getById("unit-test-config2").get().getConnection()
            .createStatement()
            .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))"));
        anc.close();
    }

    @Test
    public void getDefaultConnection() {

        Database db2 = new Database();
        db2.setConfigurationId("config");
        db2.setName("unit-test-config2");
        db2.setDefault(true);

        AnnotationConfigApplicationContext anc = new AnnotationConfigApplicationContext();
        SimpleDatabaseConnector dbc = initWithDatabaseStream(anc, db2);
        Optional<? extends DataSource> ds = dbc.getDefault();
        assertTrue(ds.isPresent());
        anc.close();
    }

    public SimpleDatabaseConnector initWithDatabaseStream(AnnotationConfigApplicationContext anc, Database... dbs) {
        anc.setParent(applicationContext);
        
        ConfigurationLoader configurationLoader = mock(ConfigurationLoader.class);
        ConfigurationStore store = mock(ConfigurationStore.class);
        when(store.getDatabase()).thenReturn(Stream.of(dbs));
        when(configurationLoader.getConfigurationStore()).thenReturn(store);

        anc.registerBean(ConfigurationLoader.class, () -> configurationLoader);
        anc.registerBean(SimpleDatabaseConnector.class);

        anc.refresh();

        return anc.getBean(SimpleDatabaseConnector.class);

    }

    @Configuration
    @Import({
        SimpleDatabaseConnector.class,
    })
    @MockBean({
        ObjectMapper.class,
    })
    public static class Conf {

        @Bean
        public ConfigurationLoader configurationLoader() {
            ConfigurationLoader configurationLoader = mock(ConfigurationLoader.class);
            ConfigurationStore store = mock(ConfigurationStore.class);
            when(store.getDatabase()).thenReturn(Stream.empty());
            when(configurationLoader.getConfigurationStore()).thenReturn(store);
            return configurationLoader;
        }
    }
    
}
