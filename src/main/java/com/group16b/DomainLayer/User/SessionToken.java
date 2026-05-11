package com.group16b.DomainLayer.User;

import java.util.UUID;//used to generate a random string

public class SessionToken {
    private final String value;

    public SessionToken() {
        this.value = UUID.randomUUID().toString(); //create a random long string of characters
    }

    public SessionToken(String existingTokenValue) {
        if (existingTokenValue == null || existingTokenValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Session token cannot be empty!");
        }
        this.value = existingTokenValue;
    }

    public String getValue() {
        return value;
    }

}