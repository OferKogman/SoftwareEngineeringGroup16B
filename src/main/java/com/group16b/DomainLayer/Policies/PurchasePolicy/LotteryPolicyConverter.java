package com.group16b.DomainLayer.Policies.PurchasePolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LotteryPolicyConverter implements AttributeConverter<LotteryPolicy, String> {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Override
    public String convertToDatabaseColumn(LotteryPolicy policy) {
        if (policy == null) {
            return null;
        }

        try {
            return MAPPER.writeValueAsString(policy);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize LotteryPolicy", e);
        }
    }

    @Override
    public LotteryPolicy convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            return MAPPER.readValue(dbData, LotteryPolicy.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize LotteryPolicy", e);
        }
    }
}