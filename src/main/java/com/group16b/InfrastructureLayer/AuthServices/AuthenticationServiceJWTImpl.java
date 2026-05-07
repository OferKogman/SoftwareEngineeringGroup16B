package com.group16b.InfrastructureLayer.AuthServices;

import java.util.Date;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class AuthenticationServiceJWTImpl implements IAuthenticationService 
{

    private final long userExpirationTime = 1000 * 60 * 60; // 1 hour
    private final long adminExpirationTime = 1000 * 60 * 15; // 15 minutes
    
    private final SecretKey Userkey;
    private final SecretKey Adminkey;
    public AuthenticationServiceJWTImpl(@Value("${jwt.userSecret}") String userSecret, @Value("${jwt.adminSecret}") String adminSecret) {
        this.Userkey = Keys.hmacShaKeyFor(userSecret.getBytes());
        this.Adminkey = Keys.hmacShaKeyFor(adminSecret.getBytes());
    }

    public String GenerateUserToken(int userID) {
        return Jwts.builder()
                .setSubject(String.valueOf(userID))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+userExpirationTime))
                .signWith(Userkey)
                .compact();
    }

    public String GenerateAdminToken(int adminID) {
        return Jwts.builder()
                .setSubject(String.valueOf(adminID))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+adminExpirationTime))
                .signWith(Adminkey)
                .compact();
    }

    public boolean authenticate(String token) {
        Jwts.parserBuilder().setSigningKey(Userkey).build().parseClaimsJws(token);
        return true;
    }
    public boolean authenticateAdmin(String token) {
        Jwts.parserBuilder().setSigningKey(Adminkey).build().parseClaimsJws(token);
        return true;
    }

    public int extractIdFromUserToken(String token) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(Userkey).build().parseClaimsJws(token);
        return Integer.parseInt(claims.getBody().getSubject());
    }

    public int extractIdFromAdminToken(String token) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(Adminkey).build().parseClaimsJws(token);
        return Integer.parseInt(claims.getBody().getSubject());
    }
}
