package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;

public class UserLoginService {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginService.class);

    private final IRepository<User> userRepository;
    private final IAuthenticationService tokenService;

    public UserLoginService(IRepository<User> userRepository, IAuthenticationService tokenService) {
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

    public Result<String> loginMember(String userID, String password) {
        logger.info("Attempting login for user ID: ...", userID);
        

        try{
            User member = userRepository.findByID(userID);
            if (!member.confirmPassword(password) || !member.getEmail().equals(userID)) {
                logger.warn("Login failed: invalid password and email attempt for user ID {}", userID);
                return Result.makeFail("Invalid user ID or password + email");
            }

            String token = tokenService.generateVisitor_SignedToken(userID);
            logger.info("user ID {} successfully logged in", userID);
            
            return Result.makeOk(token);
        }
        catch (Exception e) {
            logger.error("Login failed for user ID {}. Error: {}", userID, e.getMessage(), e);
            return Result.makeFail("Failed to login: " + e.getMessage());
        }
    }

    public Result<String> logOutMember(String sessionToken) {
        try {
            String recievedID = tokenService.extractSubjectFromToken(sessionToken);
            logger.info("Attempting log out user ID: {}...", recievedID);
            
            if (!tokenService.isUserToken(sessionToken)) {
                logger.warn("Logout failed: the session want of user for ID {}", recievedID);
                return Result.makeFail("Invalid ID for logout");
            }

            userRepository.findByID(recievedID); //check if user exists, if not will throw exception and fail logout

            Result<String> res = this.createGuestSession();

            if(res.isSuccess()){
                logger.info("user ID {} successfully logged out", recievedID);                
            }
            
            return res;
        } catch (Exception e) {
            logger.error("failed to log out in this session: {}", e.getMessage(), e);
            return Result.makeFail("Failed to log out: " + e.getMessage());
        }   
    }
}