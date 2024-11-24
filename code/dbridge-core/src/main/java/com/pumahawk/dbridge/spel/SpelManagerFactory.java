package com.pumahawk.dbridge.spel;

import com.pumahawk.dbridge.script.ScriptManager;
import com.pumahawk.dbridge.script.ScriptManagerFactory;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

@Component
public class SpelManagerFactory implements ScriptManagerFactory {

  @Autowired private SpelExpressionParser spelExpressionParser;

  @Autowired private Supplier<EvaluationContext> evaluationContext;

  @Override
  public ScriptManager getScriptManager() {
    return new SpelManager();
  }

  private class SpelManager implements ScriptManager {

    private final EvaluationContext context;

    public SpelManager() {
      context = evaluationContext.get();
    }

    @Override
    public void setVariable(String name, Object variable) {
      context.setVariable(name, variable);
    }

    @Override
    public Object getVariable(String name) {
      return context.lookupVariable(name);
    }

    @Override
    public <T> T evaluate(Class<T> type, String expression) {
      return spelExpressionParser.parseExpression(expression).getValue(context, type);
    }

    @Override
    public String evaluateTemplate(String expression) {
      return spelExpressionParser
          .parseExpression(expression, new TemplateParserContext())
          .getValue(context, String.class);
    }
  }
}
