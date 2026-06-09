package com.group16b.ApplicationLayer.Exceptions;

public class RefundStatusUnknownException extends RuntimeException {
    public RefundStatusUnknownException(String message) {
        super(message);
    }

    public RefundStatusUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
