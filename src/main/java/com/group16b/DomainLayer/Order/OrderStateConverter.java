package com.group16b.DomainLayer.Order;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderStateConverter implements AttributeConverter<OrderState, String> {

    private static final String ACTIVE_PREFIX = "ACTIVE:";
    private static final String COMPLETED = "COMPLETED";
    private static final String CANCELED = "CANCELED";
    private static final ObjectMapper mapper = buildMapper();

private static ObjectMapper buildMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(OrderState.class)
                .build();
        ObjectMapper m = new ObjectMapper();
        m.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        
        m.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        
        m.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        
        m.disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        return m;
    }

    @Override
    public String convertToDatabaseColumn(OrderState state) {
        if (state == null) {
            return null;
        }

        if (state instanceof ActiveOrder activeOrder) {
            return ACTIVE_PREFIX + activeOrder.getCreationTime();
        }

        if (state instanceof CompletedOrder) {
            return COMPLETED;
        }

        if (state instanceof CanceledOrder) {
            return CANCELED;
        }

        throw new IllegalArgumentException("Unknown OrderState type: " + state.getClass().getName());
    }

    @Override
    public OrderState convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        if (dbData.startsWith(ACTIVE_PREFIX)) {
            long creationTime = Long.parseLong(dbData.substring(ACTIVE_PREFIX.length()));
            return new ActiveOrder(creationTime);
        }

        if (dbData.equals(COMPLETED)) {
            return new CompletedOrder();
        }

        if (dbData.equals(CANCELED)) {
            return new CanceledOrder();
        }

        throw new IllegalArgumentException("Unknown OrderState database value: " + dbData);
    }
}