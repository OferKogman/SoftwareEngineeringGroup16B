package com.group16b.DomainLayer.DomainServices;

import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

public class AuthenticationService {
    private final Key secretUserKey;
    private final Key secretAdminKey;

    public AuthenticationService(String secretUserKey, String secretAdminKey) {
        // Generate secret keys for signing JWTs
        this.secretUserKey = Keys.hmacShaKeyFor(secretUserKey.getBytes());
        this.secretAdminKey = Keys.hmacShaKeyFor(secretAdminKey.getBytes());
    }

    public User authenticate(SessionToken  sessionToken) {
        // Implement authentication logic here
        return null; // Placeholder return statement
    }

}
