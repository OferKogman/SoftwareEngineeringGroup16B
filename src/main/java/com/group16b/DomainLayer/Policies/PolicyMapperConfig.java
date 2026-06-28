package com.group16b.DomainLayer.Policies;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class PolicyMapperConfig {

    public static final ObjectMapper MAPPER = buildMapper();

    private static ObjectMapper buildMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("com.group16b.DomainLayer.Policies") 
                .allowIfBaseType("java.util.Set")
                .build();
                
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        
        m.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        m.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        m.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        return m;
    }
}