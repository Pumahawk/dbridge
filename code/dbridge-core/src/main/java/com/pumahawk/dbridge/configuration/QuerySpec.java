package com.pumahawk.dbridge.configuration;

import java.util.LinkedList;
import java.util.List;

import org.springframework.http.HttpMethod;

public class QuerySpec implements Spec {

    private String path;
    private List<Query> queries = new LinkedList<>();
    private List<Validator> validators;
    private Schema schema;
    private List<HttpMethod> methods = new LinkedList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<HttpMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<HttpMethod> methods) {
        this.methods = methods;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public List<Validator> getValidators() {
        return validators;
    }

    public void setValidators(List<Validator> validators) {
        this.validators = validators;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
