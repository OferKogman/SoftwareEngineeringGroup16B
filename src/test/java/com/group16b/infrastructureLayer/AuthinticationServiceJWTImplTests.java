package com.group16b.infrastructureLayer;


import java.beans.Transient;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.group16b.InfrastructureLayer.AuthServices.AuthenticationServiceJWTImpl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

public class AuthinticationServiceJWTImplTests {
    private AuthenticationServiceJWTImpl authService;
    private String userSecret;
    private String adminSecret;
    @BeforeEach
    public void setup() {
        this.userSecret = "mySuperSecretKeyForUsers123456789"; // Must be at least 256 bits for HS256
        this.adminSecret = "mySuperSecretKeyForAdmins123456789"; // Must be at least 256 bits for HS256
        authService = new AuthenticationServiceJWTImpl(userSecret, adminSecret);
    }

    @Test
    public void testUserTokenGenerationAndAuthentication() {
        int userID = 42;
        String token = authService.GenerateUserToken(userID);
        Assertions.assertNotNull(token, "Generated token should not be null");
        Assertions.assertTrue(authService.authenticate(token), "Token should be valid");
        Assertions.assertEquals(userID, authService.extractIdFromUserToken(token), "Extracted user ID should match the original");
    }

    @Test
    public void testAdminTokenGenerationAndAuthentication() {
        int adminID = 1;
        String token = authService.GenerateAdminToken(adminID);
        Assertions.assertNotNull(token, "Generated token should not be null");
        Assertions.assertTrue(authService.authenticateAdmin(token), "Token should be valid");
        Assertions.assertEquals(adminID, authService.extractIdFromAdminToken(token), "Extracted admin ID should match the original");
    }

    @Test
    public void testInvalidTokenAuthentication() {
        String invalidToken = "invalid.token.string";
        Assertions.assertThrows(JwtException.class, () -> authService.authenticate(invalidToken), "Invalid token should not be authenticated");
        Assertions.assertThrows(JwtException.class, () -> authService.authenticateAdmin(invalidToken), "Invalid token should not be authenticated as admin");
    }

    @Test
    public void testExpiredUserTokenAuthentication()
    {
        String expiredToken= Jwts.builder()
                .setSubject("42")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // Issued 1 hour ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 30)) // Expired 30 minutes ago
                .signWith(Keys.hmacShaKeyFor(userSecret.getBytes()))
                .compact();
        Assertions.assertThrows(JwtException.class, () -> authService.authenticate(expiredToken), "Expired token should not be authenticated");
    }
        @Test
    public void testExpiredAdminTokenAuthentication()
    {
        String expiredToken= Jwts.builder()
                .setSubject("42")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // Issued 1 hour ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 30)) // Expired 30 minutes ago
                .signWith(Keys.hmacShaKeyFor(adminSecret.getBytes()))
                .compact();
        Assertions.assertThrows(JwtException.class, () -> authService.authenticateAdmin(expiredToken), "Expired token should not be authenticated as admin");
    }
    @Test
    public void testUserTokenTampering() {
        int userID = 42;
        String token = authService.GenerateUserToken(userID);
        // Tamper with the token by changing a character
        String tamperedToken = token.substring(0, token.length() - 1) + "X";
        Assertions.assertThrows(JwtException.class, () -> authService.authenticate(tamperedToken), "Tampered token should not be authenticated");
        Assertions.assertThrows(JwtException.class, () -> authService.authenticateAdmin(tamperedToken), "Tampered token should not be authenticated as admin");
    }
    @Test
    public void testAdminTokenTampering() {
        int adminID = 1;
        String token = authService.GenerateAdminToken(adminID);
        // Tamper with the token by changing a character
        String tamperedToken = token.substring(0, token.length() - 1) + "X";
        Assertions.assertThrows(JwtException.class, () -> authService.authenticate(tamperedToken), "Tampered token should not be authenticated");
        Assertions.assertThrows(JwtException.class, () -> authService.authenticateAdmin(tamperedToken), "Tampered token should not be authenticated as admin");
    }

    @Test
    public void testExtractIdFromInvalidToken() {
        String invalidToken = "invalid.token.string";
        Assertions.assertThrows(JwtException.class, () -> authService.extractIdFromUserToken(invalidToken), "Extracting ID from invalid token should throw an exception");
        Assertions.assertThrows(JwtException.class, () -> authService.extractIdFromAdminToken(invalidToken), "Extracting ID from invalid token should throw an exception");
    }

    @Test
    public void testWrongSecretKey() {
        SecretKey otherey=Keys.secretKeyFor(SignatureAlgorithm.HS256);

        String token = Jwts.builder()
                .setSubject("42")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(otherey)
                .compact();
        Assertions.assertThrows(JwtException.class, () -> authService.authenticate(token), "Token with wrong secret key should not be authenticated");
        Assertions.assertThrows(JwtException.class, () -> authService.authenticateAdmin(token), "Token with wrong secret key should not be authenticated as admin");
    }
}
