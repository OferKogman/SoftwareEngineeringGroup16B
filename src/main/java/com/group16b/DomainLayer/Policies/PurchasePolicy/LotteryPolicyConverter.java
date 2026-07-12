package com.group16b.DomainLayer.Policies.PurchasePolicy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LotteryPolicyConverter implements AttributeConverter<LotteryPolicy, String> {

    private static final ObjectMapper mapper = buildMapper();

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }

    @Override
    public String convertToDatabaseColumn(LotteryPolicy policy) {
        if (policy == null) return null;
        try {
            return mapper.writeValueAsString(policy);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize LotteryPolicy", e);
        }
    }

    @Override
    public LotteryPolicy convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return mapper.readValue(dbData, LotteryPolicy.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize LotteryPolicy", e);
        }
    }
}