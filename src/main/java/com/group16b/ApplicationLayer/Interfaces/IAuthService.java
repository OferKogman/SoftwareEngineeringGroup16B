package com.group16b.ApplicationLayer.Interfaces;

/*
the service interface for authentication
will allow treating admin and user seperetly for security reasons
will issue the tokens and validate them
will also act as a parser to extract info from the tokens when needed
*/
public interface IAuthService {
        String authenticateUser(String username, String password);
        String authenticateAdmin(String username, String password);
        boolean validateUserToken(String token);
        boolean validateAdminToken(String token);

        String extractIdFromUserToken(String token);
        String extractIdFromAdminToken(String token);
}
