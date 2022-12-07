package com.pumahawk.dbridge.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@SpringBootTest
public class QueryResourceTests {
    
    @Autowired
    private YAMLMapper yamlMapper;
    
    @Test
    public void completeMapping() throws StreamReadException, DatabindException, IOException {
        QueryResource queryResource = yamlMapper.readValue(getConfigFile("query-complete.yaml"), QueryResource.class);
        assertEquals(Kind.QUERY, queryResource.getKind());
        assertEquals("query-complete", queryResource.getMetadata().getName());
        QuerySpec spec = queryResource.getSpec();
        assertEquals("/users/{id:[0-9]+}", spec.getPath());
        assertEquals("#parse('/users/byId.vm')", spec.getQuery().getSql());
        List<HttpMethod> methods = spec.getMethods();
        assertEquals(2, methods.size());
        assertEquals(HttpMethod.GET, methods.get(0));
        assertEquals(HttpMethod.POST, methods.get(1));

        Schema schema = spec.getSchema();
        assertTrue(schema.isSchema());
        assertEquals("#foundFirst(#group(#input, 'ID'), 'User not found')", schema.getInput());
        assertEquals(4, schema.getFields().size());

        Schema id = schema.getFields().get("id");
        assertFalse(id.isSchema());
        assertEquals("#input['ID']", id.getValue());

        Schema name = schema.getFields().get("name");
        assertFalse(name.isSchema());
        assertEquals("#input['NAME']", name.getValue());

        Schema articles = schema.getFields().get("articles");
        assertTrue(articles.isSchema());
        assertEquals("#input.nested.?[#this['ARTICLE_ID'] != null]", articles.getInput());
        Schema articleId = articles.getFields().get("id");
        assertEquals(0, articleId.getFields().size());
        assertEquals("#input['ARTICLE_ID']", articleId.getValue());
        assertNull(articleId.getInput());
        Schema articleTitle = articles.getFields().get("title");
        assertEquals(0, articleTitle.getFields().size());
        assertEquals("#input['ARTICLE_TITLE']", articleTitle.getValue());
        assertNull(articleTitle.getInput());

        Schema links = schema.getFields().get("links");
        assertTrue(links.isSchema());
        assertEquals(1, links.getFields().size());
        Schema self = links.getFields().get("self");
        assertFalse(self.isSchema());
        assertEquals(0, self.getFields().size());
        assertEquals("'/users/' + #input['ID']", self.getValue());


        List<Validator> validators = spec.getValidators();

        assertEquals(1, validators.size());
        assertEquals("byId", validators.get(0).getExtends());
        
    }

    @Test
    public void specialKeys() throws StreamReadException, DatabindException, IOException {
        QueryResource queryResource = yamlMapper.readValue(getConfigFile("special-keys.yaml"), QueryResource.class);
        QuerySpec spec = queryResource.getSpec();
        assertEquals("/path", spec.getPath());
        assertEquals("sql_value", spec.getQuery().getSql());
        Schema sc = spec.getSchema();
        assertEquals(2, sc.getFields().size());

        assertEquals("'simple value'", sc.getFields().get("value").getValue());
        assertEquals("'field1'", sc.getFields().get("fields").getFields().get("field1").getValue());
    }


    private File getConfigFile(String file) {
        return new File(getClass().getResource("query/" + file).getPath());
    }
}
