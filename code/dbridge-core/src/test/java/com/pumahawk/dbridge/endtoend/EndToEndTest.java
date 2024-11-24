package com.pumahawk.dbridge.endtoend;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = {BlogPostgresEndToEndTests.Conf.class},
    properties = {
      "configuration.path=classpath:/endtoend/config",
      "velocity.file.resource.loader.path=target/test-classes/endtoend/velocity-template",
    })
@EnabledIfSystemProperty(named = "test.endtoend", matches = "true")
@Tag("postgres")
public @interface EndToEndTest {}
