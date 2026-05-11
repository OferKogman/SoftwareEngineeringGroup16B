package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;

public class UserLoginService {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginService.class);

    private final IUserRepository userRepository;
    private final IAuthenticationService tokenService;

    public UserLoginService(IUserRepository userRepository, IAuthenticationService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public Result<String> createGuestSession() {
        logger.info("Attempt to create a new guest session...");
        try {
            SessionToken newSession = new SessionToken();
            String token = tokenService.generateVisitor_GuestToken(newSession);
            
            logger.info("Successfully created guest session!!");
            return Result.makeOk(token);
            
        } catch (Exception e) {
            logger.error("Failed to create guest session. Error: ", e.getMessage(), e);
            return Result.makeFail("Failed to create guest session: " + e.getMessage());
        }
    }

    public Result<String> loginMember(int userID, String password, String email) {
        logger.info("Attempting login for user ID: ...", userID);
        
        if (!userRepository.userExists(userID)) {
            logger.warn("Login failed: user ID {} does not exist!", userID);
            return Result.makeFail("Invalid user ID");
        }

        User member = userRepository.getUserByID(userID);
        try{
            if (!member.confirmPassword(password) || !member.getEmail().equals(email)) {
                logger.warn("Login failed: invalid password and email attempt for user ID {}", userID);
                return Result.makeFail("Invalid user ID or password + email");
            }

            String token = tokenService.generateVisitor_SignedToken(userID);
            logger.info("user ID {} successfully logged in", userID);
            
            return Result.makeOk(token);
        }
        catch (Exception e) {
            logger.error("Login failed for user ID {}. Error: ", userID, e.getMessage(), e);
            return Result.makeFail("Failed to login: " + e.getMessage());
        }
    }
}