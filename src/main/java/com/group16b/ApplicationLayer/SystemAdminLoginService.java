package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.SessionToken;
import org.springframework.stereotype.Service;

@Service
public class SystemAdminLoginService {

    private static final Logger logger = LoggerFactory.getLogger(SystemAdminLoginService.class);
    private final IRepository<SystemAdmin> systemAdminRespotiry;
    private final IAuthenticationService tokenService;

    public SystemAdminLoginService(IRepository<SystemAdmin> systemAdminRespotiry, IAuthenticationService tokenService) {
        this.systemAdminRespotiry = systemAdminRespotiry;
        this.tokenService = tokenService;
    }

    public Result<String> loginAdmin(String adminUsername, String password, String email) {
        logger.info("SystemAdminLoginService.loginAdmin: Attempting login for admin username: ...", adminUsername);

        SystemAdmin admin = systemAdminRespotiry.findByID(adminUsername);

        try{
            if (!admin.confirmPassword(password) || !admin.getEmail().equals(email)) {
                logger.warn("SystemAdminLoginService.loginAdmin: Login failed: invalid password and email attempt for username {}", adminUsername);
                return Result.makeFail("invalid password or email");
            }

            String token = tokenService.generateAdminToken(adminUsername);
            logger.info("SystemAdminLoginService.loginAdmin: admin {} successfully logged in", adminUsername);
            
            return Result.makeOk(token);
        }
        catch(IllegalArgumentException e) {
            logger.warn("SystemAdminLoginService.loginAdmin: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch(Exception e) {
            logger.error("SystemAdminLoginService.loginAdmin: undexpected exception: " + e.getMessage());
            return Result.makeFail("undexpected exception " + e.getMessage());
        }
    }

    public Result<String> logOutAdmin(String sessionToken) {
        try {
            logger.info("SystemAdminLoginService.logOutAdmin: Attempting log out admin");
            if(!tokenService.validateToken(sessionToken))
            {
                logger.warn("SystemAdminLoginService.logOutAdmin: Logout attempt failed, invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please refresh your session and try again.");
            }
            if (!tokenService.isAdminToken(sessionToken)) {
                logger.warn("SystemAdminLoginService.logOutAdmin: not admin token");
                return Result.makeFail("invalid Session for logout");
            }
            String recievedusername = String.valueOf(tokenService.extractSubjectFromToken(sessionToken));

            systemAdminRespotiry.findByID(recievedusername); //check if the admin exists in the system, if not, fail the logout attempt
            
            return Result.makeOk(tokenService.generateVisitor_GuestToken(new SessionToken()));
        } catch (IllegalArgumentException e) {
            logger.warn("SystemAdminLoginService.logOutAdmin: IllegalArgumentException: ", e.getMessage());
            return Result.makeFail("Failed to log out: " + e.getMessage());
        } catch (Exception e) {
            logger.error("SystemAdminLoginService.logOutAdmin: unexpected exception: ", e.getMessage());
            return Result.makeFail("Failed to log out: " + e.getMessage());
        }   
    }

}
