package com.pumahawk.dbridge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import com.pumahawk.dbridge.configuration.Validator;
import com.pumahawk.dbridge.configuration.ValidatorRule;
import com.pumahawk.dbridge.exceptions.BadRequestParameterExpeption;

@Component
public class ValidatorManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SpelExpressionParser spelExpressionParser;

    public void validate(Validator validator, EvaluationContext context) {
        Object originalInput = context.lookupVariable("input");
        Object input = validator.getInput() != null
            ? spelExpressionParser.parseExpression(validator.getInput()).getValue(context)
            : context.lookupVariable("input");
        context.setVariable("input", input);
        ValidatorRule vc = validator.getValidator();
        if (vc != null) {
            Boolean valid = spelExpressionParser
                .parseExpression(vc.getSpel())
                .getValue(context, Boolean.class);
            if (valid == null || !valid.booleanValue()) {
                String message = "Bad Request";
                try {
                    message = spelExpressionParser.parseExpression(vc.getMessage(), new TemplateParserContext()).getValue(context, String.class);
                } catch (Exception e) {
                    logger.error("Unable to render message response", e);
                }
                throw new BadRequestParameterExpeption(message);
            }
        }
        
        try {
            if (validator.getConvert() != null) {
                spelExpressionParser.parseExpression(validator.getConvert()).getValue(context);
            } else {
                context.lookupVariable("input");
            }
        } finally {
            context.setVariable("input", originalInput);
        }
    }
    
}
