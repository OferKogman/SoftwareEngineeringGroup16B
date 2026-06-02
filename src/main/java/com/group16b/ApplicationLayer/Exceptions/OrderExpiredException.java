package com.group16b.ApplicationLayer.Exceptions;

public class OrderExpiredException extends IllegalStateException {

    public OrderExpiredException(String message) {
        super(message);
    }

}