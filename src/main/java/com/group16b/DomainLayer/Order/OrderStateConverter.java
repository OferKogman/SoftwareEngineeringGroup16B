package com.group16b.DomainLayer.Order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderStateConverter implements AttributeConverter<OrderState, String> {

    private static final ObjectMapper mapper = buildMapper();

    private static ObjectMapper buildMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(OrderState.class)
                .build();
        ObjectMapper m = new ObjectMapper();
        m.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        return m;
    }

    @Override
    public String convertToDatabaseColumn(OrderState state) {
        if (state == null) return null;
        try {
            return mapper.writeValueAsString(state);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize OrderState", e);
        }
    }

    @Override
    public OrderState convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return mapper.readValue(dbData, OrderState.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize OrderState", e);
        }
    }
}