package com.pumahawk.dbridge.util;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SimpleDatabaseConnector implements DatabaseConnector {

  @Autowired private Environment environment;

  @Autowired private ConfigurationLoader configurationLoader;

  public Map<String, HikariDataSource> dbConnections = new HashMap<>();

  private Optional<HikariDataSource> defaultConnection = Optional.empty();

  @PostConstruct
  public void initDatabaseConnectors() {
    configurationLoader
        .getConfigurationStore()
        .getDatabase()
        .forEach(
            db -> {
              HikariDataSource hds = configure(db.getConfigurationId());
              dbConnections.put(db.getName(), hds);
              if (db.isDefault()) {
                defaultConnection = Optional.of(hds);
              }
            });
  }

  @PreDestroy
  public void closeDatabaseConnections() {
    dbConnections.values().forEach(c -> c.close());
  }

  protected HikariDataSource configure(String configurationId) {
    return Binder.get(environment)
        .bindOrCreate(configurationId, Bindable.of(HikariDataSource.class));
  }

  @Override
  public Optional<DataSource> getById(String name) {
    return Optional.ofNullable(dbConnections.get(name));
  }

  @Override
  public Optional<? extends DataSource> getDefault() {
    return defaultConnection;
  }
}
