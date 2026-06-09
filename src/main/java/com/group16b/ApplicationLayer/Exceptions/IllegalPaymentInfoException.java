package com.group16b.ApplicationLayer.Exceptions;

public class IllegalPaymentInfoException extends RuntimeException {
    public IllegalPaymentInfoException(String msg)
    {
        super(msg);
    }

    public IllegalPaymentInfoException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
