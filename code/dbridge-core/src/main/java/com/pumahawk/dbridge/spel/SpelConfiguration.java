package com.pumahawk.dbridge.spel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Configuration
@EnableConfigurationProperties
public class SpelConfiguration {

  @Autowired private ApplicationContext applicationContext;

  @Bean
  public Supplier<EvaluationContext> evaluationContext() {
    return this::createEvaluationContext;
  }

  @Bean
  public SpelExpressionParser spelExpressionParser() {
    return new SpelExpressionParser();
  }

  @Bean
  @ConfigurationProperties("spel.methods")
  public Map<String, String> spelMethodsDeclaration() {
    return new HashMap<>();
  }

  @Bean
  public List<SpelMethodsDeclaration> spelMethodsDeclarationClazz(
      Map<String, String> spelMethodsDeclaration) {
    List<SpelMethodsDeclaration> l = new ArrayList<>();
    spelMethodsDeclaration.forEach((k, v) -> l.add(new SpelMethodsDeclaration(k, v)));
    ;
    return l;
  }

  public EvaluationContext createEvaluationContext() {
    try {
      StandardEvaluationContext cx = new StandardEvaluationContext();
      cx.setBeanResolver(getBeanResolver());

      @SuppressWarnings("unchecked")
      List<SpelMethodsDeclaration> spelMethodsDeclarationClazz =
          (List<SpelMethodsDeclaration>) applicationContext.getBean("spelMethodsDeclarationClazz");
      for (SpelMethodsDeclaration sc : spelMethodsDeclarationClazz) {
        cx.registerFunction(sc.name, sc.clazz.getMethod(sc.methodName, sc.args));
      }
      return cx;
    } catch (Exception e) {
      throw new RuntimeException("Unable to retrieve spel context.", e);
    }
  }

  private BeanResolver getBeanResolver() {
    return new BeanFactoryResolver(applicationContext);
  }

  public static class SpelMethodsDeclaration {
    public final String name;
    public final Class<?> clazz;
    public final String methodName;
    public final Class<?>[] args;

    public SpelMethodsDeclaration(String name, String property) {
      try {
        this.name = name;
        String[] info = property.split(",");
        clazz = Class.forName(info[0]);
        methodName = info[1];
        args = new Class<?>[info.length - 2];
        for (int i = 2; i < info.length; i++) {
          args[i - 2] = Class.forName(info[i]);
        }
      } catch (Exception e) {
        throw new RuntimeException("unable to extract class from property " + name, e);
      }
    }
  }
}
