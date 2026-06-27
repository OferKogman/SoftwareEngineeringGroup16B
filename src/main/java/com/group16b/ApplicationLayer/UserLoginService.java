package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;
import org.springframework.stereotype.Service;
import com.group16b.ApplicationLayer.Interfaces.INotificationService;

@Service
public class UserLoginService {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginService.class);

    private final IRepository<User> userRepository;
    private final IAuthenticationService tokenService;
    private final INotificationService notificationService;

    public UserLoginService(IRepository<User> userRepository, IAuthenticationService tokenService, INotificationService notificationService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.notificationService = notificationService;
    }

    public Result<String> ensureGuestSession(String SessionToken) {
        logger.info("UserLoginService.createGuestSession: Attempting to create a new guest session.");
        try {
            if(SessionToken!=null && tokenService.validateToken(SessionToken))
                return Result.makeOk(SessionToken);

            SessionToken newSession = new SessionToken();
            String token = tokenService.generateVisitor_GuestToken(newSession);
            
            logger.info("UserLoginService.createGuestSession: Successfully created guest session.");
            return Result.makeOk(token);
            
        } catch (IllegalArgumentException e) {
            logger.warn("UserLoginService.createGuestSession: Invalid session data - {}", e.getMessage());
            return Result.makeFail("Failed to create guest session: " + e.getMessage());
        } catch (Exception e) {
            logger.error("UserLoginService.createGuestSession: Unexpected error occurred - {}", e.getMessage(), e);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<String> loginMember(String userID, String password, String guestSessionToken) {
        logger.info("userLoginService.loginMember: verifying login attempt is made by a guest");
        if(!tokenService.validateToken(guestSessionToken))
        {
            logger.warn("UserLoginService.loginMember: Login attempt failed, invalid or expired guest session token.");
            return Result.makeFail("Authentication failed. Please refresh your session and try again.");
        }
        if(!tokenService.isGuestToken(guestSessionToken))
        {
            logger.warn("UserLoginService.loginMember: Login attempt failed, provided token is not a guest session token.");
            return Result.makeFail("Authentication failed. Only guests are allowed to login.");
        }

        logger.info("UserLoginService.loginMember: Attempting login for user ID {}", userID);
        
        try {
            User member = userRepository.findByID(userID);

            if (!member.confirmPassword(password)) {
                logger.warn("UserLoginService.loginMember: Login failed due to invalid password for user ID {}", userID);
                return Result.makeFail("Invalid user ID or password");
            }

            String token = tokenService.generateVisitor_SignedToken(userID);
            logger.info("UserLoginService.loginMember: User ID {} successfully logged in.", userID);

            notificationService.notify(userID, "Hello");
            return Result.makeOk(token);

        } catch (IllegalArgumentException e) {
            logger.warn("UserLoginService.loginMember: User not found - {}", e.getMessage());
            return Result.makeFail("Invalid user ID or password"); 
        } catch (Exception e) {
            logger.error("UserLoginService.loginMember: Unexpected error for user ID {} - {}", userID, e.getMessage(), e);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<String> logOutMember(String sessionToken) {
        logger.info("UserLoginService.logOutMember: Attempting to log out session.");
        try {
            if (!tokenService.isUserToken(sessionToken)) {
                logger.warn("UserLoginService.logOutMember: Logout failed, token is not a valid user session.");
                return Result.makeFail("Invalid token for logout");
            }

            String recievedID = tokenService.extractSubjectFromToken(sessionToken);
            
            userRepository.findByID(recievedID); 
            Result<String> res = this.ensureGuestSession(null);
            
            if (res.isSuccess()) {
                logger.info("UserLoginService.logOutMember: User ID {} successfully logged out.", recievedID);                
            }
            return res;
            
        } catch (IllegalArgumentException e) {
            logger.warn("UserLoginService.logOutMember: Invalid token data or user not found - {}", e.getMessage());
            return Result.makeOk(safeGenerateGusetToken());
        } catch (Exception e) {
            logger.error("UserLoginService.logOutMember: Unexpected error - {}", e.getMessage(), e);
            return Result.makeOk(safeGenerateGusetToken());
        }   
    }

    private String safeGenerateGusetToken()
    {
        SessionToken newSession = new SessionToken();
        return tokenService.generateVisitor_GuestToken(newSession);
    }
}