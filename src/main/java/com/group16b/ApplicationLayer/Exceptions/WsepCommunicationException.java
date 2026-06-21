package com.group16b.ApplicationLayer.Exceptions;

public class WsepCommunicationException extends RuntimeException{
        public WsepCommunicationException(String message) {
        super(message);
    }

    public WsepCommunicationException(String message, Throwable cause) {
        super(message, cause);
    } 
    
}
