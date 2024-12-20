package com.pumahawk.dbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(
    exclude = {
      DataSourceAutoConfiguration.class,
    })
@PropertySource({"classpath:spel.properties"})
public class DBridgeCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(DBridgeCoreApplication.class, args);
  }
}
