package com.group16b.DomainLayer.ProductionCompany;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group16b.DomainLayer.ProductionCompany.membership.MembershipNode;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class InvitesConverter implements AttributeConverter<HashMap<ProductionCompany.InviteKey, MembershipNode>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String SEPARATOR = ":";

    @Override
    public String convertToDatabaseColumn(HashMap<ProductionCompany.InviteKey, MembershipNode> attribute) {
        if (attribute == null) return null;
        try {
            Map<String, MembershipNode> flatMap = new HashMap<>();
            for (Map.Entry<ProductionCompany.InviteKey, MembershipNode> entry : attribute.entrySet()) {
                String flatKey = entry.getKey().getTargetId() + SEPARATOR + entry.getKey().getAssignerId();
                flatMap.put(flatKey, entry.getValue());
            }
            return mapper.writeValueAsString(flatMap);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize invites map", e);
        }
    }

    @Override
    public HashMap<ProductionCompany.InviteKey, MembershipNode> convertToEntityAttribute(String dbData) {
        if (dbData == null) return new HashMap<>();
        try {
            Map<String, MembershipNode> flatMap = mapper.readValue(dbData, new TypeReference<Map<String, MembershipNode>>() {});
            HashMap<ProductionCompany.InviteKey, MembershipNode> result = new HashMap<>();
            for (Map.Entry<String, MembershipNode> entry : flatMap.entrySet()) {
                String[] parts = entry.getKey().split(SEPARATOR, 2);
                result.put(new ProductionCompany.InviteKey(parts[0], parts[1]), entry.getValue());
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize invites map", e);
        }
    }
}