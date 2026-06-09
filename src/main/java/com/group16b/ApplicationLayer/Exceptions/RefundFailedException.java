package com.group16b.ApplicationLayer.Exceptions;

public class RefundFailedException extends RuntimeException {
    public RefundFailedException(String message) {
        super(message);
    }

    public RefundFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
