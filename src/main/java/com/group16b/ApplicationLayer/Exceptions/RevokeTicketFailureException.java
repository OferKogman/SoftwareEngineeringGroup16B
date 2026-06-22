package com.group16b.ApplicationLayer.Exceptions;

public class RevokeTicketFailureException extends RuntimeException {
    public RevokeTicketFailureException(String message) {
        super(message);
    }

    public RevokeTicketFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
