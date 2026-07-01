package com.group16b.DomainLayer.Order;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderStateConverter implements AttributeConverter<OrderState, String> {

    private static final String ACTIVE_PREFIX = "ACTIVE:";
    private static final String COMPLETED = "COMPLETED";
    private static final String CANCELED = "CANCELED";

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