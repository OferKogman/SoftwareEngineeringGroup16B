package com.group16b.InfrastructureLayer;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.User.SessionToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class AuthenticationServiceJWTImpl implements IAuthenticationService {

	private final long userExpirationTime = 1000 * 60 * 60; // 1 hour
	private final long adminExpirationTime = 1000 * 60 * 15; // 15 minutes

	@Value(value = "${jwt.secret}")
	private SecretKey userKey;
    private SecretKey adminKey;

    public AuthenticationServiceJWTImpl(@Value("${jwt.secret}") String userSecret, @Value("${jwt.secret}") String adminSecret) {
        this.userKey = Keys.hmacShaKeyFor(userSecret.getBytes());
        this.adminKey = Keys.hmacShaKeyFor(adminSecret.getBytes());
    }

    private String createToken(String subject, String role, long expirationMillis, SecretKey key) {
        return Jwts.builder()
                .setSubject(subject)//subject is either id or sessionToken for guest
                .claim("role", role)//Guest, Signed, Admin
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    //attempt to recieve data from admin or user, the two possible keys for token
    private Claims getAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(userKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e1) {//isnt of user type so we will try admin type
            return Jwts.parserBuilder()
                .setSigningKey(adminKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } 
    }
	
	public String generateVisitor_GuestToken(SessionToken session) {
        return createToken(session.getValue(), "Guest", userExpirationTime, userKey);
    }

    public String generateVisitor_SignedToken(int userID) {
        return createToken(String.valueOf(userID), "Signed", userExpirationTime, userKey);
    }

    public String generateAdminToken(int adminID) {
        return createToken(String.valueOf(adminID), "Admin", adminExpirationTime, adminKey);
    }


    @Override
    public boolean validateToken(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractRoleFromToken(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("role", String.class);
    }

    public String extractSubjectFromToken(String token) {
        Claims claims = getAllClaims(token);
        return claims.getSubject();
    }

    @Override
    public boolean isUserToken(String token){
        try {
            return this.extractRoleFromToken(token).equals("User");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isGuestToken(String token){
        try {
            return this.extractRoleFromToken(token).equals("Guest");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isAdminToken(String token){
        try {
            return this.extractRoleFromToken(token).equals("Admin");
        } catch (Exception e) {
            return false;
        }
    }

}
