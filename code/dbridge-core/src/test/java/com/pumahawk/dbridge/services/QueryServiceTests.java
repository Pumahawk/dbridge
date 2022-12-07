package com.pumahawk.dbridge.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Stubber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.expression.EvaluationContext;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pumahawk.dbridge.configuration.QueryResource;
import com.pumahawk.dbridge.configuration.QuerySpec;
import com.pumahawk.dbridge.configuration.Schema;
import com.pumahawk.dbridge.configuration.Validator;
import com.pumahawk.dbridge.exceptions.NotFoundRoute;
import com.pumahawk.dbridge.util.ConfigurationLoader;
import com.pumahawk.dbridge.util.ConfigurationStore;
import com.pumahawk.dbridge.util.DatabaseConnector;
import com.pumahawk.dbridge.util.SchemaManager;
import com.pumahawk.dbridge.util.ValidatorManager;

@SpringBootTest
@MockBean({
    DatabaseConnector.class
})
public class QueryServiceTests {
    
    @MockBean
    private ConfigurationLoader configurationLoader;

    @MockBean
    private ValidatorManager validatorManager;

    @MockBean
    private SqlQueryExecutor sqlQueryExecutor;

    @MockBean
    private SchemaManager schemaManager;

    @Autowired
    private QueryService queryService;

    @Test
    @SuppressWarnings("unchecked")
    public void query() {

        String path = "/users/{id}";
        
        QueryResource queryResource = new QueryResource();
        QuerySpec spec = new QuerySpec();
        spec.setPath(path);
        Validator val = new Validator();
        spec.setValidators(Collections.singletonList(val));
        Schema sc = new Schema();
        spec.setSchema(sc);
        queryResource.setSpec(spec);
        Stream<QueryResource> queries = Stream.of(queryResource);
        ConfigurationStore store = mock(ConfigurationStore.class);
        when(store.getQueries()).thenReturn(queries);
        when(configurationLoader.getConfigurationStore()).thenReturn(store);

        QueryParameter parameters = mock(QueryParameter.class);
        Map<String, Object> params = Collections.singletonMap("limit", 1);
        when(parameters.params()).thenAnswer(i -> params);
        when(parameters.path()).thenReturn("/users/442345");

        List<Object[]> validatorManagerValidateArguments = new LinkedList<>();
        collectArgumentsVoid(validatorManagerValidateArguments).when(validatorManager).validate(any(), any());
        List<Object[]> sqlExecutorQueryArguments = new LinkedList<>();
        List<Map<String, String>> result = Collections.singletonList(Collections.singletonMap("MESSAGE", "Hello, World!"));
        when(sqlQueryExecutor.query(any(), any())).then( i-> {
            sqlExecutorQueryArguments.add(i.getArguments());
            return result;
        });

        List<Object[]> schemaManagerProcessArguments = new LinkedList<>();
        when(schemaManager.process(any(), any())).thenAnswer(i -> {
            schemaManagerProcessArguments.add(i.getArguments());
            return new ObjectMapper().createObjectNode().put("message", "hello");
        });
        
        QueryResult qr = queryService.query(parameters);

        assertEquals(1, validatorManagerValidateArguments.size());
        Validator vl = (Validator) validatorManagerValidateArguments.get(0)[0];
        assertEquals(val, vl);
        EvaluationContext cx = (EvaluationContext) validatorManagerValidateArguments.get(0)[1];
        Map<String, Object> pr = (Map<String, Object>) cx.lookupVariable("p");
        if (pr != null) {
            assertEquals(1, pr.get("limit"));
            assertEquals("442345", pr.get("id"));
        } else {
            throw new RuntimeException("pr must not be null");
        }

        assertEquals(1, sqlExecutorQueryArguments.size());
        assertEquals(spec, sqlExecutorQueryArguments.get(0)[0]);
        pr = (Map<String, Object>) sqlExecutorQueryArguments.get(0)[1];
        if (pr != null) {
            assertEquals(1, pr.get("limit"));
            assertEquals("442345", pr.get("id"));
        } else {
            throw new RuntimeException("pr must not be null");
        }

        assertEquals(1, schemaManagerProcessArguments.size());
        assertEquals(sc, schemaManagerProcessArguments.get(0)[0]);
        assertEquals(result, schemaManagerProcessArguments.get(0)[1]);

        assertEquals("hello", qr.getData().get("message").asText());
    }

    @Test
    public void notFound() {
        
        String path = "/users/{id}";
        QueryResource queryResource = new QueryResource();
        QuerySpec spec = new QuerySpec();
        spec.setPath(path);
        Validator val = new Validator();
        spec.setValidators(Collections.singletonList(val));
        Schema sc = new Schema();
        spec.setSchema(sc);
        queryResource.setSpec(spec);
        Stream<QueryResource> queries = Stream.of(queryResource);
        ConfigurationStore store = mock(ConfigurationStore.class);
        when(store.getQueries()).thenReturn(queries);
        when(configurationLoader.getConfigurationStore()).thenReturn(store);

        QueryParameter parameters = mock(QueryParameter.class);
        Map<String, Object> params = Collections.singletonMap("limit", 1);
        when(parameters.params()).thenAnswer(i -> params);
        when(parameters.path()).thenReturn("/articles/442345");

        assertThrows(NotFoundRoute.class, () -> queryService.query(parameters));
    }

    @Test
    public void useDifferentMethod() {

        // Query Spec POST
        // Request method GET

        String path = "/users/{id}";
        
        QueryResource queryResource = new QueryResource();
        QuerySpec spec = new QuerySpec();
        spec.setPath(path);
        spec.setMethods(Collections.singletonList(HttpMethod.POST));
        Validator val = new Validator();
        spec.setValidators(Collections.singletonList(val));
        Schema sc = new Schema();
        spec.setSchema(sc);
        queryResource.setSpec(spec);
        Stream<QueryResource> queries = Stream.of(queryResource);
        ConfigurationStore store = mock(ConfigurationStore.class);
        when(store.getQueries()).thenReturn(queries);
        when(configurationLoader.getConfigurationStore()).thenReturn(store);

        QueryParameter parameters = mock(QueryParameter.class);
        Map<String, Object> params = Collections.singletonMap("limit", 1);
        when(parameters.params()).thenAnswer(i -> params);
        when(parameters.method()).thenReturn(HttpMethod.GET);
        when(parameters.path()).thenReturn("/users/442345");

        List<Object[]> validatorManagerValidateArguments = new LinkedList<>();
        collectArgumentsVoid(validatorManagerValidateArguments).when(validatorManager).validate(any(), any());
        List<Object[]> sqlExecutorQueryArguments = new LinkedList<>();
        List<Map<String, String>> result = Collections.singletonList(Collections.singletonMap("MESSAGE", "Hello, World!"));
        when(sqlQueryExecutor.query(any(), any())).then( i-> {
            sqlExecutorQueryArguments.add(i.getArguments());
            return result;
        });

        List<Object[]> schemaManagerProcessArguments = new LinkedList<>();
        when(schemaManager.process(any(), any())).thenAnswer(i -> {
            schemaManagerProcessArguments.add(i.getArguments());
            return new ObjectMapper().createObjectNode().put("message", "hello");
        });
        
        assertThrows(NotFoundRoute.class, () -> queryService.query(parameters));
        
    }

    private Stubber collectArgumentsVoid(List<Object[]> collectorArguments) {
        return collectArgumentsVoid(collectorArguments, null);
    }
    
    private Stubber collectArgumentsVoid(List<Object[]> collectorArguments, Object ret) {
        return doAnswer(i -> {
            collectorArguments.add(i.getArguments());
            return ret;
        });
    }
}
