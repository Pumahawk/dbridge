package com.pumahawk.dbridge.configuration;

public class QueryResource extends BasicResource<QuerySpec> {

    @Override
    public Kind getKind() {
        return Kind.QUERY;
    }
}
