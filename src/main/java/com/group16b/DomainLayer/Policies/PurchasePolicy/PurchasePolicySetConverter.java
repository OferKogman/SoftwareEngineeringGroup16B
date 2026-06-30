package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.group16b.DomainLayer.Policies.PolicyMapperConfig;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PurchasePolicySetConverter implements AttributeConverter<Set<PurchasePolicy>, String> {

    @Override
    public String convertToDatabaseColumn(Set<PurchasePolicy> policies) {
        if (policies == null || policies.isEmpty()) return "[]";
        try {
            return PolicyMapperConfig.MAPPER.writeValueAsString(policies);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize PurchasePolicies", e);
        }
    }

    @Override
    public Set<PurchasePolicy> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty() || dbData.equals("[]")) return new HashSet<>();
        try {
            return PolicyMapperConfig.MAPPER.readValue(dbData, new TypeReference<Set<PurchasePolicy>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize PurchasePolicies", e);
        }
    }
}
