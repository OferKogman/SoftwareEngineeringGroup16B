package com.group16b.infrastructureLayer;

import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
		String token = authService.generateVisitor_SignedToken(userID);
		Assertions.assertNotNull(token, "generated token should not be null");
		Assertions.assertTrue(authService.validateToken(token), "Token should be valid");
		Assertions.assertEquals(userID, Integer.valueOf(authService.extractSubjectFromToken(token)));
	}

	@Test
	public void testAdminTokenGenerationAndAuthentication() {
		int adminID = 1;
		String token = authService.generateAdminToken(adminID);
		Assertions.assertNotNull(token, "generated token should not be null");
		Assertions.assertTrue(authService.validateToken(token), "Token should be valid");
		Assertions.assertEquals(adminID, Integer.valueOf(authService.extractSubjectFromToken(token)));
	}

	@Test
	public void testInvalidTokenAuthentication() {
		String invalidToken = "invalid.token.string";
		assertEquals(false, authService.validateToken(invalidToken));
	}

	@Test
	public void testExpiredUserTokenAuthentication() {
		String expiredToken = Jwts.builder()
				.setSubject("42")
				.setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // Issued 1 hour ago
				.setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 30)) // Expired 30 minutes ago
				.signWith(Keys.hmacShaKeyFor(userSecret.getBytes()))
				.compact();
		assertEquals(false, authService.validateToken(expiredToken));
	}

	@Test
	public void testExpiredAdminTokenAuthentication() {
		String expiredToken = Jwts.builder()
				.setSubject("42")
				.setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // Issued 1 hour ago
				.setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 30)) // Expired 30 minutes ago
				.signWith(Keys.hmacShaKeyFor(adminSecret.getBytes()))
				.compact();
		assertEquals(false, authService.validateToken(expiredToken));
	}

	@Test
	public void testUserTokenTampering() {
		int userID = 42;
		String token = authService.generateVisitor_SignedToken(userID);
		// Tamper with the token by changing a character
		String tamperedToken = token.substring(0, token.length() - 1) + "X";
		assertEquals(false, authService.validateToken(tamperedToken));
	}

	@Test
	public void testAdminTokenTampering() {
		int adminID = 1;
		String token = authService.generateAdminToken(adminID);
		// Tamper with the token by changing a character
		String tamperedToken = token.substring(0, token.length() - 1) + "X";
		assertEquals(false, authService.validateToken(tamperedToken));
	}

	@Test
	public void testExtractIdFromInvalidToken() {
		String invalidToken = "invalid.token.string";
		Assertions.assertThrows(JwtException.class, () -> 
            authService.extractSubjectFromToken(invalidToken));
	}

	@Test
	public void testWrongSecretKey() {
		SecretKey otherey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

		String token = Jwts.builder()
				.setSubject("42")
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
				.signWith(otherey)
				.compact();
				
		assertEquals(false, authService.validateToken(token));
	}
}
