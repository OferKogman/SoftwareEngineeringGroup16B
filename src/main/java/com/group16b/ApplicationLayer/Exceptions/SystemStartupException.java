package com.group16b.ApplicationLayer.Exceptions;

public class SystemStartupException extends RuntimeException {
    public SystemStartupException(String message) {
        super(message);
    }

    public SystemStartupException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
