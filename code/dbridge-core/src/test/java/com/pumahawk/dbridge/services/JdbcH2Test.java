package com.pumahawk.dbridge.services;

import com.pumahawk.dbridge.util.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class JdbcH2Test {

  @MockitoBean protected DatabaseConnector databaseConnector;

  private DataSource dataSource;
  private Connection connection;

  @BeforeEach
  public void initDatabase() {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setUrl("jdbc:h2:mem:");
    dataSource = ds;
  }

  @AfterEach
  public void closeDatabase() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public DataSource getDataSource() {
    try {
      return new SingleConnectionDataSource(getConnection(), true);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      connection = dataSource.getConnection();
    }
    return connection;
  }
}
