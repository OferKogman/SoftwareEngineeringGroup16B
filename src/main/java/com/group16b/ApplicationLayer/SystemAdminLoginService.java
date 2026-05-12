package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.SessionToken;

public class SystemAdminLoginService {

    private static final Logger logger = LoggerFactory.getLogger(SystemAdminLoginService.class);
    private final ISystemAdminRepository systemAdminRespotiry;
    private final IAuthenticationService tokenService;

    public SystemAdminLoginService(ISystemAdminRepository systemAdminRespotiry, IAuthenticationService tokenService) {
        this.systemAdminRespotiry = systemAdminRespotiry;
        this.tokenService = tokenService;
    }

    public Result<String> loginAdmin(int adminID, String password, String email) {
        logger.info("Attempting login for admin ID: ...", adminID);

        SystemAdmin admin = systemAdminRespotiry.getSystemAdminById(adminID);

        if (admin == null) {
            logger.warn("Login failed: user ID {} does not exist!", adminID);
            return Result.makeFail("Invalid admin ID");
        }

        try{
            if (!admin.confirmPassword(password) || !admin.getEmail().equals(email)) {
                logger.warn("Login failed: invalid password and email attempt for user ID {}", adminID);
                return Result.makeFail("Invalid user ID or password + email");
            }

            String token = tokenService.generateAdminToken(adminID);
            logger.info("admin ID {} successfully logged in", adminID);
            
            return Result.makeOk(token);
        }
        catch (Exception e) {
            logger.error("Login failed for admin ID {}. Error: ", adminID, e.getMessage(), e);
            return Result.makeFail("Failed to login: " + e.getMessage());
        }
    }

    public Result<String> logOutAdmin(String sessionToken) {
        try {
            int recievedID = Integer.valueOf(tokenService.extractSubjectFromToken(sessionToken));
            logger.info("Attempting log out admin ID: {}...", recievedID);
            
            if (!tokenService.isAdminToken(sessionToken)) {
                logger.warn("Logout failed: the session want of admin for ID {}", recievedID);
                return Result.makeFail("Invalid ID for logout");
            }

            if (!systemAdminRespotiry.doesSystemAdminExist(recievedID)){
                logger.warn("Logout failed: user ID {} of the token does not exist!", recievedID);
                return Result.makeFail("Invalid adminID ID");
            }
            
            return Result.makeOk(tokenService.generateVisitor_GuestToken(new SessionToken()));
        } catch (Exception e) {
            logger.error("failed to log out in this session: {}", e.getMessage(), e);
            return Result.makeFail("Failed to log out: " + e.getMessage());
        }   
    }

}
