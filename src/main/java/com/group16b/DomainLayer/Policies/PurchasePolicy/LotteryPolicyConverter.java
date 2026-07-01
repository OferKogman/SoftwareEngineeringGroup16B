package com.group16b.DomainLayer.Policies.PurchasePolicy;

import com.group16b.DomainLayer.Policies.PolicyMapperConfig;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LotteryPolicyConverter implements AttributeConverter<LotteryPolicy, String> {

    @Override
    public String convertToDatabaseColumn(LotteryPolicy policy) {
        if (policy == null) return null;
        try {
            return PolicyMapperConfig.MAPPER.writeValueAsString(policy);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize LotteryPolicy", e);
        }
    }

    @Override
    public LotteryPolicy convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return PolicyMapperConfig.MAPPER.readValue(dbData, LotteryPolicy.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize LotteryPolicy", e);
        }
    }
}