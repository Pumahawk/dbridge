package com.pumahawk.dbridge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.pumahawk.dbridge.configuration.Validator;
import com.pumahawk.dbridge.configuration.ValidatorRule;
import com.pumahawk.dbridge.exceptions.BadRequestParameterExpeption;
import com.pumahawk.dbridge.script.ScriptManager;

@Component
public class ValidatorManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void validate(Validator validator, ScriptManager scriptManager) {
        Object originalInput = scriptManager.getVariable("input");
        Object input = validator.getInput() != null
            ? scriptManager.evaluate(validator.getInput())
            : scriptManager.getVariable("input");
        scriptManager.setVariable("input", input);
        ValidatorRule vc = validator.getValidator();
        if (vc != null) {
            Boolean valid = scriptManager.evaluate(Boolean.class, vc.getSpel());
            if (valid == null || !valid.booleanValue()) {
                String message = "Bad Request";
                try {
                    message = scriptManager.evaluateTemplate(vc.getMessage());
                } catch (Exception e) {
                    logger.error("Unable to render message response", e);
                }
                throw new BadRequestParameterExpeption(message);
            }
        }
        
        try {
            if (validator.getConvert() != null) {
                scriptManager.evaluate(validator.getConvert());
            } else {
                scriptManager.getVariable("input");
            }
        } finally {
            scriptManager.setVariable("input", originalInput);
        }
    }
    
}
