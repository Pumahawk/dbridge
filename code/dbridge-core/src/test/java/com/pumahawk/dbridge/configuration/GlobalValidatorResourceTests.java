package com.pumahawk.dbridge.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@SpringBootTest
public class GlobalValidatorResourceTests {

    @Autowired
    private YAMLMapper yamlMapper;
    
    @Test
    public void completeMapping() throws StreamReadException, DatabindException, IOException {

        GlobalValidatorResource globalValidatorResource = yamlMapper.readValue(getConfigFile("global-validator-complete.yaml"), GlobalValidatorResource.class);
        assertEquals(Kind.GLOBAL_VALIDATOR, globalValidatorResource.getKind());
        assertEquals("basic-id-validators", globalValidatorResource.getMetadata().getName());

        GlobalValidatorSpec spec = globalValidatorResource.getSpec();
        List<GlobalValidator> globalValidators = spec.getGlobalValidators();
        assertEquals(1, globalValidators.size());

        GlobalValidator globalValidator = globalValidators.get(0);
        assertEquals("byId", globalValidator.getName());
        assertEquals(1, globalValidator.getValidators().size());

        Validator validator = globalValidator.getValidators().get(0);
        assertEquals("id", validator.getName());
        assertEquals("#p['id'] = #toNumber(#input)", validator.getConvert());
        assertEquals(null, validator.getExtends());
        assertEquals("#p['id']", validator.getInput());
        ValidatorRule rule = validator.getValidator();
        assertEquals("Id must be a valid number. Current value: #{#input}", rule.getMessage());
        assertEquals("#input eq null || #isNumber(#input)", rule.getSpel());
        

    }

    private File getConfigFile(String file) {
        return new File(getClass().getResource("global-validator/" + file).getPath());
    }
}
