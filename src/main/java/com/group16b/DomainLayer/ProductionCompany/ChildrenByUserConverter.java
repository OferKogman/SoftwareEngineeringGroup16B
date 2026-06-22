package com.group16b.DomainLayer.ProductionCompany;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ChildrenByUserConverter implements AttributeConverter<Map<String, Set<String>>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Set<String>> attribute) {
        if (attribute == null) return null;
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize childrenByUser map", e);
        }
    }

    @Override
    public Map<String, Set<String>> convertToEntityAttribute(String dbData) {
        if (dbData == null) return new HashMap<>();
        try {
            return mapper.readValue(dbData, new TypeReference<Map<String, Set<String>>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize childrenByUser map", e);
        }
    }
}