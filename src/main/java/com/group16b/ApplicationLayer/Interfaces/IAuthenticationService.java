package com.group16b.ApplicationLayer.Interfaces;

import com.group16b.DomainLayer.User.SessionToken;

/*
the service interface for authentication
will allow treating admin and user seperetly for security reasons
will issue the tokens and validate them
will also act as a parser to extract info from the tokens when needed
*/
public interface IAuthenticationService {
	boolean validateToken(String token);

	String generateVisitor_GuestToken(SessionToken session);

	String generateVisitor_SignedToken(int userID);

	String generateAdminToken(int adminID);

	String extractRoleFromToken(String token);

	String extractSubjectFromToken(String token);

	boolean isUserToken(String token);
}
