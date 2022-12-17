package com.pumahawk.dbridge.services;

import java.io.StringWriter;
import java.util.HashMap;
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
import com.pumahawk.dbridge.script.ScriptManager;
import com.pumahawk.dbridge.script.ScriptManagerFactory;
import com.pumahawk.dbridge.util.DatabaseConnector;
import com.pumahawk.dbridge.util.NamedSupporter;

@Component
public class SqlQueryExecutor {

    @Autowired
    private DatabaseConnector databaseConnector;

    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private ScriptManagerFactory scriptManagerFactory;

    public Map<String, ? extends Object> query(QuerySpec spec, Map<String, Object> requestParams) {
        NamedSupporter<Object> namedSupporter = new NamedSupporter<>(requestParams.entrySet());
        QueryContext queryContext = new QueryContext(namedSupporter);
        int i = -1;
        for (Query q : spec.getQueries()) {
            i++;
            String name = q.getName() != null ? q.getName() : "_q" + i;
            Object input = queryContext.spel(q.getInput());
            queryContext.put(false, "input", input);
            String sql = queryContext.render(q.getSql());
            Object resultQuery = executeQuery(q.isUpdate(), q.getDatabase(), sql, namedSupporter.getStore());
            queryContext.put(true, name, resultQuery);
            if (q.getConversion() != null) {
                resultQuery = queryContext.spel(q.getConversion());
                queryContext.put(true, name, resultQuery);
            }
        }
        return queryContext.getResult();
    }

    private Object executeQuery(boolean update, String database, String sql, Map<String, Object> properties) {
        NamedParameterJdbcTemplate jdbc = getJdbc(database);
        return update
            ? jdbc.update(sql, properties)
            : jdbc.queryForList(sql, properties);
    }

    private Object solveExpression(String expression, ScriptManager scriptManager) {
        return expression != null
          ? scriptManager.evaluate(expression)
          : null;
    }

    private String renderQuery(String sql, VelocityContext context) {
        Assert.notNull(sql, "sql parameter is mandatory");
        StringWriter sw = new StringWriter();
        velocityEngine.evaluate(context, sw, "render simple query", sql);
        return sw.toString();
    }

    private VelocityContext createContext(NamedSupporter<Object> namedSupporter) {
        VelocityContext context = new VelocityContext();
        namedSupporter.forEach(en -> context.put(en.getKey(), en.getValue()));
        context.put("_", namedSupporter);
        return context;
    }

    private NamedParameterJdbcTemplate getJdbc(String database) {
        Optional<? extends DataSource> opt = Optional.ofNullable(database)
            .flatMap(databaseConnector::getById);

        DataSource ds = opt.isPresent()
            ? opt.get()
            : databaseConnector.getDefault().orElseThrow(() -> new RuntimeException("Unable to get find datasource."));
        
        return new NamedParameterJdbcTemplate(ds);
    }

    private static class SqlQueryResult extends HashMap<String, Object> {
    }

    private class QueryContext {
        
        private final SqlQueryResult result = new SqlQueryResult();
        private final VelocityContext velocityContext;
        private final ScriptManager scriptManager;

        public  QueryContext(NamedSupporter<Object> namedSupporter) {
            this.velocityContext = createContext(namedSupporter);
            this.scriptManager = scriptManagerFactory.getScriptManager();
        }


        public String render(String velocityTemplate) {
            return renderQuery(velocityTemplate, getVelocityContext());
        }


        public Object spel(String spelExpression) {
            return solveExpression(spelExpression, getScriptManager());
        }


        public void put(boolean setResult, String key, Object value) {
            result.put(key, value);
            velocityContext.put(key, value);
            scriptManager.setVariable(key, value);
            if (setResult) {
                result.put("result", value);
                velocityContext.put("result", value);
                scriptManager.setVariable("result", value);
            }
        }

        public SqlQueryResult getResult() {
            return result;
        }

        public VelocityContext getVelocityContext() {
            return velocityContext;
        }

        public ScriptManager getScriptManager() {
            return scriptManager;
        }

    }


}
