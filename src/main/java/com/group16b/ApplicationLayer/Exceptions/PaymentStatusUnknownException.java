package com.group16b.ApplicationLayer.Exceptions;

public class PaymentStatusUnknownException extends RuntimeException {
    public PaymentStatusUnknownException(String message) {
        super(message);
    }

    public PaymentStatusUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
