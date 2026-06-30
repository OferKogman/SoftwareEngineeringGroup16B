package com.group16b.DomainLayer.Policies.DiscountPolicy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.group16b.DomainLayer.Policies.PolicyMapperConfig;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashSet;
import java.util.Set;

@Converter
public class DiscountPolicySetConverter implements AttributeConverter<Set<DiscountPolicy>, String> {

    @Override
    public String convertToDatabaseColumn(Set<DiscountPolicy> policies) {
        if (policies == null || policies.isEmpty()) return "[]";
        try {
            return PolicyMapperConfig.MAPPER.writeValueAsString(policies);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize DiscountPolicies", e);
        }
    }

    @Override
    public Set<DiscountPolicy> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty() || dbData.equals("[]")) return new HashSet<>();
        try {
            return PolicyMapperConfig.MAPPER.readValue(dbData, new TypeReference<Set<DiscountPolicy>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize DiscountPolicies", e);
        }
    }
}