package com.group16b.ApplicationLayer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.RoleType;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

import io.jsonwebtoken.JwtException;

public class CompanyHierarchyService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final IAuthenticationService authenticationService;
    private final CompanyHierarchyDomainService companyHierarchyDomainService;
	private final IUserRepository userRepository = UserRepositoryMapImpl.getInstance();

	private final ConcurrentHashMap<Integer, Object> companyLocks = new ConcurrentHashMap<>();

    public CompanyHierarchyService(IAuthenticationService authenticationService, CompanyHierarchyDomainService companyHierarchyDomainService) {
		this.authenticationService = authenticationService;
		this.companyHierarchyDomainService=companyHierarchyDomainService;
	}

    public Result<Boolean> assignOwnerToCompany(int companyID, int targetID, String sessionToken) {
		Object lock = getCompanyLock(companyID);
        try {
			//auth
			logger.info("Verifying session token for Owner assignment of user {0} to company {1}.", targetID, companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for Owner assignment of user {0} to company {1}.", targetID, companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			logger.info("Session token verified successfully.");
            if(user==null)
            {
                logger.warn("User with id {0} was not found",userID);
                return Result.makeFail("user not found");
            }

            //get target user
			logger.info("retrieving target user for Owner assignment.");
			User targetUser = userRepository.getUserByID(targetID);
			if (targetUser==null) {
				logger.warn("Target user with ID {0} not found for Owner assignment.", targetID);
				return Result.makeFail("Target user not found.");
			}
            
            synchronized(lock)
            {
                //get perms
                logger.info("Validating user permissions for owner assignment.");
                if(!user.isOwnerOfCompany(companyID))
                {
                    logger.warn("user {0} is not owner of company {1} and thus cant invite target {2} to be owner",userID,companyID,targetID);
                    return Result.makeFail("User not owner");
                }
                logger.info("User permissions validated successfully.");


                //ensure not owner already
                logger.info("ensuring target isnt already an owner for company.");
                if(targetUser.isOwnerOfCompany(companyID))
                {
                    logger.warn("target {0} is already an owner of company {1} when trying to send invite by user {2}",targetID,companyID,userID);
                    return Result.makeFail("Target already owner");
                }

                //add invite to target user
                logger.info("Adding owner assignment invite to target user.");
                targetUser.addInvite(companyID, userID, new Owner(userID));
            }
			logger.info("user {0} have been succesfully invited to be an owner in company {1} by user {2}",targetID,companyID,userID);
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to invite owner: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to invite owner: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during owner invitation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during owner invitation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	
	public Result<Boolean> assignManagerToCompany(int companyID, int targetID, Set<ManagerPermissions> permissions, String sessionToken) {
		Object lock = getCompanyLock(companyID);
        try {
			//auth
			logger.info("Verifying session token for Manager assignment of user {0} to company {1}.", targetID, companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for Manager assignment of user {0} to company {1}.", targetID, companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			logger.info("Session token verified successfully.");
            if(user==null)
            {
                logger.warn("user with ID {0} not found for Manager assignment.", userID);
				return Result.makeFail("user not found.");
            }
            //get target user
            logger.info("retrieving target user for Manager assignment.");
            User targetUser = userRepository.getUserByID(targetID);
			if (targetUser==null) {
				logger.warn("Target user with ID {0} not found for Manager assignment.", targetID);
				return Result.makeFail("Target user not found.");
			}

            logger.info("ensuring permissions are valid for sending invite to target {0}");
            if(permissions==null || permissions.isEmpty())
            {
                logger.warn("cant assing manager with empty or null set of permissions");
                return Result.makeFail("Cant assign manager with empty permissions!");
            }
            synchronized(lock)
            {
                //get perms
                logger.info("Validating user permissions for manager assignment.");
                if(!user.isOwnerOfCompany(companyID))
                {
                    logger.warn("user {0} is not owner of company {1} and thus cant invite target {2} to be manager",userID,companyID,targetID);
                    return Result.makeFail("User not owner");
                }
                logger.info("User permissions validated successfully.");

                //send invite
                logger.info("ensuring target isnt already a manager for company.");
                if(targetUser.getRole(companyID)!=null)
                {
                    logger.warn("target {0} is already an manager of company {1} when trying to send invite by user {2}",targetID,companyID,userID);
                    return Result.makeFail("Target already manager");
                }

                logger.info("Adding manager assignment invite to target user.");
                targetUser.addInvite(companyID, userID, new Manager(userID, permissions));

            }
			logger.info("user {0} have been succesfully invited to be a manager in company {1} by user {2}",targetID,companyID,userID);
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to invite manager: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to invite manager: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during inviting manager: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during inviting manager: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Boolean> acceptInviteToCompany(int companyID, int assignerID, String sessionToken) {
		Object companyLock = getCompanyLock(companyID);
		try {
			//auth
			logger.info("Verifying session token for accepting invite assignment to company {0} by assigner {2}.", companyID, assignerID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for accepting invite assignment to company {0} by assigner {1}.", companyID, assignerID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			if(user==null)
			{
				logger.warn("user {0} was not found, maybe deleted",userID);
				return Result.makeFail("user not found");
			}
			logger.info("Session token verified successfully.");

			User assigner = userRepository.getUserByID(assignerID);
			if (assigner == null) {
				logger.warn("Assigner user with ID {0} not found for accepting invite assignment to company {1} by user {2}.", assignerID, companyID, userID);
				return Result.makeFail("Assigner user not found.");
			}

			synchronized(companyLock)
			{
				if(!assigner.isOwnerOfCompany(companyID))
				{
					logger.warn("Assigner user with ID {0} does not have permission to assign roles for company {1} for accepting invite assignment to company {1} by user {2}.", assignerID, companyID, userID);
					return Result.makeFail("Assigner user does not have permission to assign roles for this company.");
				}
				
				//check that invite exists and accept it
				logger.info("accepting invite assignment invite for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
                user.acceptInvite(companyID, assignerID);
                assigner.addAssignee(companyID, (Manager) user.getRole(companyID));
                logger.info("Invite assignment invite accepted successfully for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);

			}
			logger.info("user {0} have succesfully accepted an invite to company {1} by user {2}",userID,companyID,assignerID);
			return Result.makeOk(true);
		} catch (IllegalArgumentException e) {
			logger.error("Failed to accepting invite: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to accept invite: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during invite acceptance: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during accepting invite: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Boolean> rejectInviteToCompany( int companyID, int assignerID, String sessionToken) {
		Object companyLock = getCompanyLock(companyID);
        try {
			//auth
			logger.info("Verifying session token for rejecting invite assignment to company {0} by assigner {1}.", companyID, assignerID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for rejecting invite assignment to company {0} by assigner {1}.", companyID, assignerID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
            if(user==null)
			{
				logger.warn("user {0} was not found, maybe deleted",userID);
				return Result.makeFail("user not found");
			}
			logger.info("Session token verified successfully.");
            synchronized(companyLock)
            {
                //check that invite exists and reject it
                logger.info("rejecting invite assignment for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
                user.rejectInvite(companyID, assignerID);
                logger.info("invite rejected successfully for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);

            }
			logger.info("user {0} have succesfully rejected an invite to company {1} by user {2}",userID,companyID,assignerID);
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to reject invite: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to reject invite: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during invite rejection: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during invite rejection: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

    public Result<Boolean> forfeitOwnership(int companyID, String sessionToken) {
		Object lock = getCompanyLock(companyID);
		try {
			//auth
			logger.info("Verifying session token for forfeiten ownership for company {0}.", companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for forfeiten ownership for company {0}.", companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			if (user == null) {
				logger.warn("User with ID {0} not found for forfeiting ownership", userID);
				return Result.makeFail("User not found.");
			}
			logger.info("Session token verified successfully.");
			Integer assignerID;
			synchronized(lock)
			{
				if(!user.isOwnerOfCompany(companyID))
				{
					logger.warn("user {0} is not owner for comapny {1}, thus he cant forfeit his ownership there",userID,companyID);
					return Result.makeFail("user is not owner");
				}

				assignerID=user.getParentIDForCompany(companyID);
				if(assignerID==null)
				{
					logger.warn("user {0} is founder and thus can't leave the company {1}",userID, companyID);
					return Result.makeFail("founder cant leave company");
				}
				companyHierarchyDomainService.removeUserFromCompany(user, companyID);
			}
			logger.info("user {0} has succesfuly forfeited its role in company {1}, thus removing itself from the children of its company parent {2}",userID,companyID,assignerID);
			return Result.makeOk(true);

		} catch (IllegalArgumentException | IllegalStateException e) {
			logger.error("Failed to forfeit ownership: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during forfeitng ownership: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during forfeiting ownership: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Boolean> removeOwnerManager(int targetID, int companyID, String sessionToken) {
		Object lock = getCompanyLock(companyID);
		try {
			//auth
			logger.info("Verifying session token for removing manager with id {0} for company {1}.", targetID,companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for removing manager with id {0} for company {1}.", targetID,companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			if (user == null) {
				logger.warn("User with ID {0} not found for removing manager", userID);
				return Result.makeFail("User not found.");
			}
			logger.info("Session token verified successfully.");
			logger.info("retrieving target user {0} to remove manager from company {1} by user {2}",targetID,companyID,userID);
			User target=userRepository.getUserByID(targetID);
			if(target==null)
			{
				logger.warn("target user {0} was not found to remove him from the compny {1} by user {2}.",targetID,companyID,userID);
				return Result.makeFail("target user was not found");
			}
			synchronized(lock)
			{
				Result<Boolean> canManage=canManage(user, target, companyID);
                if(!canManage.isSuccess())
                    return canManage;

				companyHierarchyDomainService.removeUserFromCompany(target, companyID);
			}
			logger.info("target {0} was removed from company {1} by user {2}",targetID,companyID,userID);
			return Result.makeOk(true);


		} catch (IllegalArgumentException | IllegalStateException e) {
			logger.error("Failed to remove manager from company: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during forfeitng ownership: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during forfeiting ownership: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> changeManagerPermission(int targetID, int companyID, Set<ManagerPermissions> newPermissions, String sessionToken) {
		Object lock = getCompanyLock(companyID);
		try {
			//auth
			logger.info("Verifying session token for changing manager permissions for target id {0} for company {1}.", targetID,companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for changing manager permissions for target id {0} for company {1}.", targetID,companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			if (user == null) {
				logger.warn("User with ID {0} not found for updating manager permissions", userID);
				return Result.makeFail("User not found.");
			}
			logger.info("Session token verified successfully.");
			logger.info("retrieving target user {0} to update his permissions from company {1} by user {2}",targetID,companyID,userID);
			User target=userRepository.getUserByID(targetID);
			if(target==null)
			{
				logger.warn("target user {0} was not found to his manager permissions from the compny {1} by user {2}.",targetID,companyID,userID);
				return Result.makeFail("target user was not found");
			}
			synchronized(lock)
			{
				Result<Boolean> canManage=canManage(user, target, companyID);
                if(!canManage.isSuccess())
                    return canManage;
                if(((Manager)target.getRole(companyID)).getRoleType()!=RoleType.MANAGER)
                {
                    logger.warn("user {0} tried to update the permissions of target {1} in company {2}, but target is owner",userID,targetID,companyID);
                    return Result.makeFail("Cannot change permissions of an owner");
                }
				companyHierarchyDomainService.updateManagerPermissionsForCompny(target, companyID, newPermissions);
			}
			logger.info("target {0} have had their manager permissions updated in company {1} by user {2}",targetID,companyID,userID);
			return Result.makeOk(true);


		} catch (IllegalArgumentException | IllegalStateException e) {
			logger.error("Failed to update manager from company: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during update manager permissions: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during update manager permissions: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

    //checks if bigDog can manage smallDog inside a company
    private Result<Boolean> canManage(User bigDog, User smallDog, int companyID)
    {
        int userID=bigDog.getUserID();
        int targetID=smallDog.getUserID();
        if(!bigDog.isOwnerOfCompany(companyID))
        {//potentially save time before expensive hierarchy traversal
            logger.warn("user {0} is not owner for comapny {1}, thus he cant manage anyone there",userID,companyID);
            return Result.makeFail("user is not owner");
        }

        if(smallDog.getRole(companyID)==null)
        {//same here, simply save time
            logger.warn("target user {0} is not personal in the company {1} so the cannot be manager there by user {2}",targetID,companyID,userID);
            return Result.makeFail("target user is not personal in company");
        }

        if(!companyHierarchyDomainService.isManagerUnderOwnerTreeTraversal(smallDog, bigDog, companyID))
        {
            logger.warn("User {0} isn't above target {1} in the hierarchy tree in company {2}, thus he cant manage them",userID,targetID,companyID);
            return Result.makeFail("User didn't apoint target so no permission to manage them");
        }
        return Result.makeOk(null);
    }

    private Object getCompanyLock(int companyID) {
		return companyLocks.computeIfAbsent(companyID, id -> new Object());
	}
    
}
