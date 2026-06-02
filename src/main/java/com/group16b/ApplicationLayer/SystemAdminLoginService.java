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
                logger.warn("SystemAdminLoginService.loginAdmin: Login failed: invalusername password and email attempt for user username {}", adminUsername);
                return Result.makeFail("Invalusername user username or password + email");
            }

            String token = tokenService.generateAdminToken(adminUsername);
            logger.info("SystemAdminLoginService.loginAdmin: admin username {} successfully logged in", adminUsername);
            
            return Result.makeOk(token);
        }
        catch(IllegalArgumentException e) {
            logger.warn("SystemAdminLoginService.loginAdmin: Login failed: user username {} does not exist!", adminUsername);
            return Result.makeFail("Invalusername user username");
        }
        catch(Exception e) {
            logger.error("SystemAdminLoginService.loginAdmin: failed to log in admin username {}: {}", adminUsername, e.getMessage(), e);
            return Result.makeFail("Failed to log in: " + e.getMessage());
        }
    }

    public Result<String> logOutAdmin(String sessionToken) {
        try {
            String recievedusername = String.valueOf(tokenService.extractSubjectFromToken(sessionToken));
            logger.info("SystemAdminLoginService.loginAdmin: Attempting log out admin username: {}...", recievedusername);
            
            if (!tokenService.isAdminToken(sessionToken)) {
                logger.warn("SystemAdminLoginService.loginAdmin: Logout failed: the session want of admin for username {}", recievedusername);
                return Result.makeFail("Invalusername username for logout");
            }

            systemAdminRespotiry.findByID(recievedusername); //check if the admin exists in the system, if not, fail the logout attempt
            
            return Result.makeOk(tokenService.generateVisitor_GuestToken(new SessionToken()));
        } catch (IllegalArgumentException e) {
            logger.warn("SystemAdminLoginService.logOutAdmin: Logout failed: invalid session token - {}", e.getMessage());
            return Result.makeFail("Failed to log out: " + e.getMessage());
        } catch (Exception e) {
            logger.error("SystemAdminLoginService.logOutAdmin: failed to log out in this session: {}", e.getMessage(), e);
            return Result.makeFail("Failed to log out: " + e.getMessage());
        }   
    }

}
