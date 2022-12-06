package com.pumahawk.dbridge.services;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.pumahawk.dbridge.configuration.Query;
import com.pumahawk.dbridge.configuration.QuerySpec;
import com.pumahawk.dbridge.util.DatabaseConnector;

@Component
public class SqlQueryExecutor {

    @Autowired
    private DatabaseConnector databaseConnector;

    @Autowired
    private VelocityEngine velocityEngine;

    public List<Map<String, Object>> query(QuerySpec spec, Map<String, Object> requestParams) {
        return getJdbc(spec).queryForList(renderQuery(spec.getQuery().getSql(), requestParams), requestParams);
    }

    private String renderQuery(String sql, Map<String, Object> queryParams) {
        Assert.notNull(sql, "sql parameter is mandatory");
        StringWriter sw = new StringWriter();
        VelocityContext context = new VelocityContext();
        queryParams.entrySet().forEach(en -> context.put(en.getKey(), en.getValue()));
        velocityEngine.evaluate(context, sw, "render simple query", sql);
        return sw.toString();
    }

    private NamedParameterJdbcTemplate getJdbc(QuerySpec spec) {
        Optional<? extends DataSource> opt = Optional.ofNullable(spec)
            .map(QuerySpec::getQuery)
            .map(Query::getDatabase)
            .flatMap(databaseConnector::getById);

        DataSource ds = opt.isPresent()
            ? opt.get()
            : databaseConnector.getDefault().orElseThrow(() -> new RuntimeException("Unable to get find datasource."));
        
        return new NamedParameterJdbcTemplate(ds);
    }


}
