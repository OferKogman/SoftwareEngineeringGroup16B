package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.User;

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

            String token = tokenService.generateVisitor_SignedToken(adminID);
            logger.info("admin ID {} successfully logged in", adminID);
            
            return Result.makeOk(token);
        }
        catch (Exception e) {
            logger.error("Login failed for admin ID {}. Error: ", adminID, e.getMessage(), e);
            return Result.makeFail("Failed to login: " + e.getMessage());
        }
    }

}
