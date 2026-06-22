package com.group16b.ApplicationLayer.Exceptions;

public class TicketRevokeUnknownStatusException extends RuntimeException {
    public TicketRevokeUnknownStatusException(String message) {
        super(message);
    }

    public TicketRevokeUnknownStatusException(String message, Throwable cause) {
        super(message, cause);
    } 
    
}
