package com.pumahawk.dbridge.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.pumahawk.dbridge.configuration.QueryResource;
import com.pumahawk.dbridge.configuration.QuerySpec;
import com.pumahawk.dbridge.configuration.Schema;
import com.pumahawk.dbridge.configuration.Validator;
import com.pumahawk.dbridge.exceptions.NotFoundRoute;
import com.pumahawk.dbridge.util.ConfigurationLoader;
import com.pumahawk.dbridge.util.SchemaManager;
import com.pumahawk.dbridge.util.ValidatorManager;

@Component
public class QueryService {

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private ValidatorManager validatorManager;

    @Autowired
    private SqlQueryExecutor sqlQueryExecutor;

    @Autowired
    private Supplier<EvaluationContext> evaluationContext;

    @Autowired
    private SchemaManager schemaManager;

    public QueryResult query(QueryParameter parameters) {

        return configurationLoader
            .getConfigurationStore()
            .getQueries()
            .map(QueryResource::getSpec)
            .filter(s -> s.getMethods().size() > 0
                ? s.getMethods().contains(parameters.method())
                : true
            )
            .filter(q -> new UriTemplate(q.getPath()).matches(parameters.path()))
            .findAny()
            .map(q -> {
                Map<String, Object> params = new HashMap<>();
                params.putAll(parameters.params());
                params.putAll(new UriTemplate(q.getPath()).match(parameters.path()));
                validateAndConvertParams(q.getValidators(), params);
                return query(q, params);
            }).orElseThrow(() -> new NotFoundRoute());
    }

    private QueryResult query(QuerySpec spec, Map<String, Object> params) {
        List<Map<String, Object>> result = sqlQueryExecutor.query(spec, params);
        JsonNode data = extractDataFromResult(spec.getSchema(), result);
        return generateResult(spec, params, data);
    }

    private QueryResult generateResult(QuerySpec spec, Map<String, Object> params, JsonNode data) {
        SimpleQueryResult sqr = new SimpleQueryResult();
        sqr.setData(data);
        return sqr;
    }

    private JsonNode extractDataFromResult(Schema schema, List<Map<String, Object>> result) {
        return schemaManager.process(schema, result);
    }

    private void validateAndConvertParams(List<Validator> validators, Map<String, Object> params) {
        EvaluationContext context = createContext(params);
        validators.forEach(v -> {
            validatorManager.validate(v, context);
        });
    }

    private EvaluationContext createContext(Map<String, Object> params) {
        EvaluationContext context = evaluationContext.get();
        context.setVariable("p", params);
        return context;
    }
}
