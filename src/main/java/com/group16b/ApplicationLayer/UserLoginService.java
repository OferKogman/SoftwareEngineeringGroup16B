package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class UserLoginService {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginService.class);

    private final IRepository<User> userRepository = new UserRepositoryMapImpl();
    private final IAuthenticationService tokenService;

    public UserLoginService(IAuthenticationService tokenService) {
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

    public Result<String> loginMember(String userEmail, String password, String email) {
        logger.info("Attempting login for user ID: ...", userEmail);
    

        try{
            User member = userRepository.findByID(userEmail);
            if (!member.confirmPassword(password) || !member.getEmail().equals(email)) {
                logger.warn("Login failed: invalid password and email attempt for user ID {}", userEmail);
                return Result.makeFail("Invalid user ID or password + email");
            }

            String token = tokenService.generateVisitor_SignedToken(userEmail);
            logger.info("user ID {} successfully logged in", userEmail);
            
            return Result.makeOk(token);
        }
        catch (Exception e) {
            logger.error("Login failed for user ID {}. Error: {}", userEmail, e.getMessage(), e);
            return Result.makeFail("Failed to login: " + e.getMessage());
        }
    }

    public Result<String> logOutMember(String sessionToken) {
        try {
            String recievedID = String.valueOf(tokenService.extractSubjectFromToken(sessionToken));
            logger.info("Attempting log out user ID: {}...", recievedID);
            
            if (!tokenService.isUserToken(sessionToken)) {
                logger.warn("Logout failed: the session want of user for ID {}", recievedID);
                return Result.makeFail("Invalid ID for logout");
            }

            userRepository.findByID(recievedID); // check if user exist, if not, exception will be thrown and caught in catch block

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