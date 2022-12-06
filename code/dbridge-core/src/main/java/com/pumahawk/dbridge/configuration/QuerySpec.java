package com.pumahawk.dbridge.configuration;

import java.util.List;

public class QuerySpec implements Spec {

    private String path;
    private Query query;
    private List<Validator> validators;
    private Schema schema;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
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
