package com.group16b.ApplicationLayer.Exceptions;

public class IllegalTicketInfoException extends RuntimeException {
    public IllegalTicketInfoException(String msg)
    {
        super(msg);
    }

    public IllegalTicketInfoException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
}
