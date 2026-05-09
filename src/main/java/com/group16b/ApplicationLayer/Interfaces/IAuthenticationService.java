package com.group16b.ApplicationLayer.Interfaces;

/*
the service interface for authentication
will allow treating admin and user seperetly for security reasons
will issue the tokens and validate them
will also act as a parser to extract info from the tokens when needed
*/
public interface IAuthenticationService {
	boolean authenticate(String token);

	boolean authenticateAdmin(String token);

	String GenerateUserToken(int userID);

	String GenerateAdminToken(int adminID);

	int extractIdFromUserToken(String token);

	int extractIdFromAdminToken(String token);
}
