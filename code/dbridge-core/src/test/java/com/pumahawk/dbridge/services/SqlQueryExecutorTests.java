package com.pumahawk.dbridge.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.pumahawk.dbridge.configuration.Query;
import com.pumahawk.dbridge.configuration.QuerySpec;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SqlQueryExecutorTests extends JdbcH2Test {

  @Autowired private SqlQueryExecutor sqlQueryExecutor;

  @Test
  public void loadContext() {}

  @Test
  public void databaseWithDefault() throws SQLException {

    when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
    assertDoesNotThrow(
        () ->
            sqlQueryExecutor.query(createQuerySpec("SELECT * FROM USERS"), Collections.emptyMap()));
  }

  @Test
  public void databaseWithName() throws SQLException {

    when(databaseConnector.getById("db1")).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
    assertDoesNotThrow(
        () ->
            sqlQueryExecutor.query(createQuerySpec("SELECT * FROM USERS"), Collections.emptyMap()));
  }

  @Test
  public void withInListParameter() throws SQLException {

    when(databaseConnector.getById("db1")).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'A')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(2, 'B')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(3, 'C')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(4, 'D')");
    LinkedList<Integer> ids = new LinkedList<>();
    ids.add(2);
    ids.add(3);

    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> result =
        (Map<String, List<Map<String, Object>>>)
            assertDoesNotThrow(
                () ->
                    sqlQueryExecutor.query(
                        createQuerySpec("SELECT * FROM USERS WHERE id in (:ids)"),
                        Collections.singletonMap("ids", ids)));

    assertEquals(2, result.get("result").size());

    assertEquals("B", result.get("result").get(0).get("NAME"));
    assertEquals("C", result.get("result").get(1).get("NAME"));
  }

  @Test
  public void doDatasourceFound() throws SQLException {
    Exception e =
        assertThrows(
            Exception.class,
            () ->
                sqlQueryExecutor.query(
                    createQuerySpec("SELECT * FROM USERS"), Collections.emptyMap()));
    assertTrue(e.getMessage().contains("Unable to get find datasource"));
  }

  @Test
  public void executeQuery() throws SQLException {

    when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1234, 'Francesco')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(2, 'Luca')");

    String sql =
        "SELECT * FROM USERS WHERE 1 = 1 #if($id) AND id = :id #end #if($name) AND NAME LIKE :name #end";
    Map<String, Object> params = Collections.singletonMap("id", "1234");
    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> result =
        (Map<String, List<Map<String, Object>>>)
            sqlQueryExecutor.query(createQuerySpec(sql), params);
    assertEquals(1, result.get("result").size());
    assertEquals(1234, result.get("result").get(0).get("ID"));
    assertEquals("Francesco", result.get("result").get(0).get("NAME"));
  }

  @Test
  public void executeQueryUsingNamedSupporter() throws SQLException {

    when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1234, 'Francesco')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(2, 'Luca')");

    String sql = "SELECT * FROM USERS WHERE ID = $_.use($id)";
    Map<String, Object> params = Collections.singletonMap("id", 1234);
    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> result =
        (Map<String, List<Map<String, Object>>>)
            sqlQueryExecutor.query(createQuerySpec(sql), params);
    assertEquals(3, result.size());
    assertNull(result.get("input"));
    assertTrue(result.containsKey("input"));
    assertEquals(1234, result.get("result").get(0).get("ID"));
    assertEquals("Francesco", result.get("result").get(0).get("NAME"));
    assertEquals(1234, result.get("_q0").get(0).get("ID"));
    assertEquals("Francesco", result.get("_q0").get(0).get("NAME"));
  }

  @Test
  public void executeMultipleQuery() throws SQLException {
    when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(2, 'Luca')");

    String sql1 = "SELECT ID FROM USERS WHERE NAME = 'Matteo'";
    String sql2 = "SELECT NAME FROM USERS WHERE ID NOT IN ($_.use($_q0[0]['ID']))";

    QuerySpec querySpec = createQuerySpec(sql1, sql2);

    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> result =
        (Map<String, List<Map<String, Object>>>)
            sqlQueryExecutor.query(querySpec, Collections.emptyMap());

    List<Map<String, Object>> rq0 = result.get("_q0");
    List<Map<String, Object>> rq1 = result.get("_q1");
    List<Map<String, Object>> resultq = result.get("result");

    assertEquals(1, rq0.size());
    assertEquals(1, rq0.get(0).get("ID"));

    assertEquals(1, rq1.size());
    assertEquals("Luca", rq1.get(0).get("NAME"));

    assertEquals(1, resultq.size());
    assertEquals("Luca", resultq.get(0).get("NAME"));
  }

  @Test
  public void executeMultipleQuery_withInput() throws SQLException {
    when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(2, 'Luca')");

    String sql1 = "SELECT ID FROM USERS WHERE NAME = 'Matteo'";

    String input = "#_q0[0]['ID']";
    String sql2 = "SELECT NAME FROM USERS WHERE ID NOT IN ($_.use($input))";

    QuerySpec querySpec = createQuerySpec(sql1, sql2);
    querySpec.getQueries().get(1).setInput(input);

    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> result =
        (Map<String, List<Map<String, Object>>>)
            sqlQueryExecutor.query(querySpec, Collections.emptyMap());

    List<Map<String, Object>> rq0 = result.get("_q0");
    List<Map<String, Object>> rq1 = result.get("_q1");
    List<Map<String, Object>> resultq = result.get("result");

    assertEquals(1, rq0.size());
    assertEquals(1, rq0.get(0).get("ID"));

    assertEquals(1, rq1.size());
    assertEquals("Luca", rq1.get(0).get("NAME"));

    assertEquals(1, resultq.size());
    assertEquals("Luca", resultq.get(0).get("NAME"));
  }

  @Test
  public void executeMultipleQuery_withConversion() throws SQLException {
    when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
    getConnection().createStatement().execute("INSERT INTO USERS VALUES(2, 'Luca')");

    String sql1 = "SELECT ID FROM USERS WHERE NAME = 'Matteo'";
    String sql2 = "SELECT NAME FROM USERS WHERE ID NOT IN ($_.use($_q0[0]['ID']))";
    String conversion = "{q0:#_q0,q1:#customq}";

    QuerySpec querySpec = createQuerySpec(sql1, sql2);
    querySpec.getQueries().get(1).setConversion(conversion);
    querySpec.getQueries().get(1).setName("customq");

    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> result =
        (Map<String, List<Map<String, Object>>>)
            sqlQueryExecutor.query(querySpec, Collections.emptyMap());

    List<Map<String, Object>> rq0 = result.get("_q0");
    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> rq1 =
        (Map<String, List<Map<String, Object>>>) result.get("customq");
    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> resultq =
        (Map<String, List<Map<String, Object>>>) result.get("result");

    assertEquals(1, rq0.size());
    assertEquals(1, rq0.get(0).get("ID"));

    assertEquals(2, rq1.size());
    assertEquals(1, rq1.get("q0").get(0).get("ID"));
    assertEquals("Luca", rq1.get("q1").get(0).get("NAME"));

    assertEquals(2, resultq.size());
    assertEquals(1, resultq.get("q0").get(0).get("ID"));
    assertEquals("Luca", resultq.get("q1").get(0).get("NAME"));
  }

  @Test
  public void insertValues() throws SQLException {
    when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

    getConnection()
        .createStatement()
        .execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");

    QuerySpec querySpec =
        createQuerySpec(
            "INSERT INTO USERS VALUES(1, 'Matteo')", "SELECT NAME FROM USERS WHERE ID = 1");

    querySpec.getQueries().get(0).setUpdate(true);

    Map<String, ? extends Object> result =
        sqlQueryExecutor.query(querySpec, Collections.emptyMap());
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> user = (List<Map<String, Object>>) result.get("result");
    assertEquals("Matteo", user.get(0).get("NAME"));
  }

  private QuerySpec createQuerySpec(String... sql) {
    QuerySpec qs = new QuerySpec();
    for (String q : sql) {
      Query query = new Query();
      query.setSql(q);
      query.setDatabase("db1");
      qs.getQueries().add(query);
    }
    return qs;
  }
}
