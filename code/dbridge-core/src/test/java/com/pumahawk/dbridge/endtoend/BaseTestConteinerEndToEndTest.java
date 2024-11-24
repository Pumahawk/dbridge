package com.pumahawk.dbridge.endtoend;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@DirtiesContext
public class BaseTestConteinerEndToEndTest {

    private static Logger logger = LoggerFactory.getLogger(BaseTestConteinerEndToEndTest.class);

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
	    	.withInitScript("endtoend/data.sql")
	        .withDatabaseName("blog");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
	logger.info("Configuration test container");
        registry.add("db.configuration.postgres.jdbcUrl", postgres::getJdbcUrl);
        registry.add("db.configuration.postgres.username", postgres::getUsername);
        registry.add("db.configuration.postgres.password", postgres::getPassword);
        registry.add("db.configuration.postgres.driverClassName", postgres::getDriverClassName);
    }
}
