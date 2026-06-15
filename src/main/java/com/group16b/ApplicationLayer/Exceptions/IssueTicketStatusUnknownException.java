package com.group16b.ApplicationLayer.Exceptions;

public class IssueTicketStatusUnknownException extends RuntimeException{
    public IssueTicketStatusUnknownException(String msg)
    {
        super(msg);
    }

    public IssueTicketStatusUnknownException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
