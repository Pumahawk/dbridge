package com.pumahawk.dbridge.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.pumahawk.dbridge.configuration.Query;
import com.pumahawk.dbridge.configuration.QuerySpec;

@SpringBootTest
public class SqlQueryExecutorTests extends JdbcH2Test {

    @Autowired
    private SqlQueryExecutor sqlQueryExecutor;

    @Test
    public void loadContext() {
    }

    @Test
    public void databaseWithDefault() throws SQLException {

        when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

        getConnection().createStatement().execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
        assertDoesNotThrow(() -> sqlQueryExecutor.query(createQuerySpec("SELECT * FROM USERS"), Collections.emptyMap()));
        
    }

    @Test
    public void databaseWithName() throws SQLException {

        when(databaseConnector.getById("db1")).thenAnswer(i -> Optional.of(getDataSource()));

        getConnection().createStatement().execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
        assertDoesNotThrow(() -> sqlQueryExecutor.query(createQuerySpec("SELECT * FROM USERS"), Collections.emptyMap()));
        
    }

    @Test
    public void withInListParameter() throws SQLException {

        when(databaseConnector.getById("db1")).thenAnswer(i -> Optional.of(getDataSource()));

        getConnection().createStatement().execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'A')");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(2, 'B')");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(3, 'C')");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(4, 'D')");
        LinkedList<Integer> ids = new LinkedList<>();
        ids.add(2);
        ids.add(3);
        List<Map<String, Object>> result = assertDoesNotThrow(() -> sqlQueryExecutor.query(createQuerySpec("SELECT * FROM USERS WHERE id in (:ids)"), Collections.singletonMap("ids", ids)));

        assertEquals(2, result.size());

        assertEquals("B", result.get(0).get("NAME"));
        assertEquals("C", result.get(1).get("NAME"));
        
    }

    @Test
    public void doDatasourceFound() throws SQLException {
        Exception e = assertThrows(Exception.class, () -> sqlQueryExecutor.query(createQuerySpec("SELECT * FROM USERS"), Collections.emptyMap()));
        assertTrue(e.getMessage().contains("Unable to get find datasource"));
    }


    @Test
    public void executeQuery() throws SQLException {

        when(databaseConnector.getDefault()).thenAnswer(i -> Optional.of(getDataSource()));

        getConnection().createStatement().execute("CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(15))");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(1, 'Matteo')");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(1234, 'Francesco')");
        getConnection().createStatement().execute("INSERT INTO USERS VALUES(2, 'Luca')");

        String sql = "SELECT * FROM USERS WHERE 1 = 1 #if($id) AND id = :id #end #if($name) AND NAME LIKE :name #end";
        Map<String, Object> params = Collections.singletonMap("id", "1234");
        List<Map<String, Object>> result = sqlQueryExecutor.query(createQuerySpec(sql), params);
        assertEquals(1, result.size());
        assertEquals(1234, result.get(0).get("ID"));
        assertEquals("Francesco", result.get(0).get("NAME"));
    }

    private QuerySpec createQuerySpec(String sql) {
        QuerySpec qs = new QuerySpec();
        Query query = new Query();
        query.setSql(sql);
        query.setDatabase("db1");
        qs.setQuery(query);
        return qs;
    }
}
