package com.group16b.ApplicationLayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
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
	private final IProductionCompanyRepository productionCompanyRepository;
	private final ConcurrentHashMap<Integer, Object> companyLocks = new ConcurrentHashMap<>();

    public CompanyHierarchyService(IAuthenticationService authenticationService, CompanyHierarchyDomainService companyHierarchyDomainService, IProductionCompanyRepository productionCompanyRepository) {
		this.authenticationService = authenticationService;
		this.companyHierarchyDomainService=companyHierarchyDomainService;
		this.productionCompanyRepository=productionCompanyRepository;
	}

    public Result<Boolean> assignOwnerToCompany(int companyID, int targetID, String sessionToken) {
		try{
			//auth
			logger.info("Verifying session token for Owner assignment of user {} to company {}.", targetID, companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for Owner assignment of user {} to company {}.", targetID, companyID);
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to assign owner.");
				return Result.makeFail("Only signed-in users are allowed to assign owners. Please use a user account.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			userRepository.getUserByID(userID);
			logger.info("Session token verified successfully.");

			logger.info("attempting to retrieve target User {}",targetID);
			userRepository.getUserByID(targetID);

			logger.info("attempting to retrieve production company {}", companyID);
			ProductionCompany company= productionCompanyRepository.findByID(String.valueOf(companyID));

			logger.info("Attempting to send owner invite to user {} by user {} in company {}",targetID,userID,companyID);
			company.AssignOwner(userID, targetID);
			logger.info("user {} Succefully invited target {} to be owner in company {}",userID,targetID,companyID);

			logger.info("attempting to save changed in production company {}",companyID);
			productionCompanyRepository.save(company);

			return Result.makeOk(true);
		}
		catch(IllegalArgumentException e)
		{
			logger.warn("Runtime error during assignOwner: "+e.getMessage());
			return Result.makeFail(e.getMessage());
		}
		catch(OptimisticLockingFailureException e)
		{
			logger.warn("Optimistic locking Failure in assign owner: "+e.getMessage());
			return Result.makeFail("Company was updated by another operation. Please retry.");
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during inviting owner: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Unexpected error during inviting owner: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}    
	}

	
	public Result<Boolean> assignManagerToCompany(int companyID, int targetID, Set<ManagerPermissions> permissions, String sessionToken) {
		try{
			//auth
			logger.info("Verifying session token for manager assignment of user {} to company {}.", targetID, companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for manager assignment of user {} to company {}.", targetID, companyID);
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to assign manager.");
				return Result.makeFail("Only signed-in users are allowed to assign managers. Please use a user account.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			userRepository.getUserByID(userID);
			logger.info("Session token verified successfully.");

			logger.info("attempting to retrieve target User {}",targetID);
			userRepository.getUserByID(targetID);

			logger.info("attempting to retrieve production company {}", companyID);
			ProductionCompany company= productionCompanyRepository.findByID(String.valueOf(companyID));

			logger.info("Attempting to send manager invite to user {} by user {} in company {}",targetID,userID,companyID);
			company.AssignManager(userID, targetID,permissions);
			logger.info("user {} Succefully invited target {} to be manager in company {}",userID,targetID,companyID);

			logger.info("attempting to save changed in production company {}",companyID);
			productionCompanyRepository.save(company);

			return Result.makeOk(true);
		}
		catch(IllegalArgumentException e)
		{
			logger.warn("Runtime error during assign Manager: "+e.getMessage());
			return Result.makeFail(e.getMessage());
		}
		catch(OptimisticLockingFailureException e)
		{
			logger.warn("Optimistic locking Failure in assign manager: "+e.getMessage());
			return Result.makeFail("Company was updated by another operation. Please retry.");
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during inviting manager: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Unexpected error during inviting manager: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}    
	}


	public Result<Boolean> acceptInviteToCompany(int companyID, int assignerID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for accepting invite assignment to company {} by assigner {}.", companyID, assignerID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for accepting invite assignment to company {} by assigner {}.", companyID, assignerID);
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to accept invite.");
				return Result.makeFail("Only signed-in users are allowed to accept invites. Please use a user account.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			userRepository.getUserByID(userID);

			logger.info("Session token verified successfully.");

			logger.info("ensuring assigner {} exists for accept invite",assignerID);
			userRepository.getUserByID(assignerID);

			logger.info("trying to retrieve company {} for accept invite",companyID);
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(companyID));

			logger.info("trying to accept invite for user {} assigner by {} in company {}",userID,assignerID,companyID);
			company.acceptInvite(userID, assignerID);


			logger.info("user {} have succesfully accepted an invite to company {} by assigner {}",userID,companyID,assignerID);

			logger.info("Trying to save change for accepting invite");
			productionCompanyRepository.save(company);
			logger.info("Succesfully saved company {} after accepting invite",companyID);
			return Result.makeOk(true);
		}
		catch(IllegalArgumentException e)
		{
			logger.warn("Runtime error during accept Invite: "+e.getMessage());
			return Result.makeFail(e.getMessage());
		}
		catch(OptimisticLockingFailureException e)
		{
			logger.warn("Optimistic locking Failure in accept Invite: "+e.getMessage());
			return Result.makeFail("Company was updated by another operation. Please retry.");
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during accept Invite: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Unexpected error during accept Invite: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}  
	}

	public Result<Boolean> rejectInviteToCompany( int companyID, int assignerID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for rejection invite assignment to company {} by assigner {}.", companyID, assignerID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for rejection invite assignment to company {} by assigner {}.", companyID, assignerID);
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to reject invite.");
				return Result.makeFail("Only signed-in users are allowed to reject invites. Please use a user account.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			userRepository.getUserByID(userID);

			logger.info("Session token verified successfully.");

			logger.info("ensuring assigner {} exists for reject invite",assignerID);
			userRepository.getUserByID(assignerID);

			logger.info("trying to retrieve company {} for reject invite",companyID);
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(companyID));

			logger.info("trying to reject invite for user {} assigner by {} in company {}",userID,assignerID,companyID);
			company.rejectInvite(userID, assignerID);


			logger.info("user {} have succesfully rejected an invite to company {} by assigner {}",userID,companyID,assignerID);

			logger.info("Trying to save change for reject invite");
			productionCompanyRepository.save(company);
			logger.info("Succesfully saved company {} after rejected invite",companyID);
			return Result.makeOk(true);
		}
		catch(IllegalArgumentException e)
		{
			logger.warn("Runtime error during reject Invite: "+e.getMessage());
			return Result.makeFail(e.getMessage());
		}
		catch(OptimisticLockingFailureException e)
		{
			logger.warn("Optimistic locking Failure in reject Invite: "+e.getMessage());
			return Result.makeFail("Company was updated by another operation. Please retry.");
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during reject Invite: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Unexpected error during accept Invite: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

    public Result<Boolean> forfeitOwnership(int companyID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for Forfeit ownership in company {}.", companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for Forfeit ownership in company {}.", companyID);
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to Forfeit ownership.");
				return Result.makeFail("Only signed-in users are allowed to Forfeit ownership. Please use a user account.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			userRepository.getUserByID(userID);

			logger.info("Session token verified successfully.");


			logger.info("trying to retrieve company {} for Forfeit ownership by user {}",companyID,userID);
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(companyID));

			logger.info("trying to Forfeit ownership by user{} in company {}",userID,companyID);
			company.forfeitOwnership(userID);


			logger.info("user {} have succesfully Forfeited ownership in company {}.",userID,companyID);

			logger.info("Trying to save change for Forfeit ownership");
			productionCompanyRepository.save(company);
			logger.info("Succesfully saved company {} after Forfeiting ownership",companyID);
			return Result.makeOk(true);
		}
		catch(IllegalArgumentException e)
		{
			logger.warn("Runtime error during Forfeit ownership: "+e.getMessage());
			return Result.makeFail(e.getMessage());
		}
		catch(OptimisticLockingFailureException e)
		{
			logger.warn("Optimistic locking Failure in Forfeit ownership: "+e.getMessage());
			return Result.makeFail("Company was updated by another operation. Please retry.");
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during Forfeit ownership: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Unexpected error during Forfeit ownership: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Boolean> removeOwnerManager(int targetID, int companyID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for remove membership in company {}.", companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for  remove membership in company {}.", companyID);
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to  remove membership.");
				return Result.makeFail("Only signed-in users are allowed to  remove membership. Please use a user account.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			userRepository.getUserByID(userID);

			logger.info("Session token verified successfully.");

			logger.info("ensuring target user {} exists",targetID);
			userRepository.getUserByID(targetID);

			logger.info("trying to retrieve company {} for remove membership of target {} by user {}",companyID, targetID,userID);
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(companyID));

			logger.info("trying to remove membership of target {} by user{} in company {}",targetID,userID,companyID);
			company.removeMemberByOwner(userID, targetID);


			logger.info("user {} have succesfully remove membership of target {} in company {}.",userID,targetID,companyID);

			logger.info("Trying to save change for remove membership of target {}",targetID);
			productionCompanyRepository.save(company);
			logger.info("Succesfully saved company {} after remove membership of target {}",companyID,targetID);
			return Result.makeOk(true);
		}
		catch(IllegalArgumentException e)
		{
			logger.warn("Runtime error during remove membership: "+e.getMessage());
			return Result.makeFail(e.getMessage());
		}
		catch(OptimisticLockingFailureException e)
		{
			logger.warn("Optimistic locking Failure in remove membership: "+e.getMessage());
			return Result.makeFail("Company was updated by another operation. Please retry.");
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during remove membership: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Unexpected error during remove membership: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> changeManagerPermission(int targetID, int companyID, Set<ManagerPermissions> newPermissions, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for update manager permissions of target {} in company {}.",targetID, companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for update manager permissions of target {} in company {}.", targetID,companyID);
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to update manager permissions.");
				return Result.makeFail("Only signed-in users are allowed to update manager permissions. Please use a user account.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			userRepository.getUserByID(userID);

			logger.info("Session token verified successfully.");

			logger.info("ensuring target user {} exists",targetID);
			userRepository.getUserByID(targetID);

			logger.info("trying to retrieve company {} for update manager permissions of target {} by user {}",companyID, targetID,userID);
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(companyID));

			logger.info("trying to update manager permissions of target {} by user{} in company {}",targetID,userID,companyID);
			company.updatePermissionsOfManager(userID, targetID, newPermissions);


			logger.info("user {} have succesfully updated manager permissions of target {} in company {}.",userID,targetID,companyID);

			logger.info("Trying to save change for update manager permissions of target {}",targetID);
			productionCompanyRepository.save(company);
			logger.info("Succesfully saved company {} after update manager permissions of target {} by user {}",companyID,targetID,userID);
			return Result.makeOk(true);
		}
		catch(IllegalArgumentException e)
		{
			logger.warn("Runtime error during update manager permissions: "+e.getMessage());
			return Result.makeFail(e.getMessage());
		}
		catch(OptimisticLockingFailureException e)
		{
			logger.warn("Optimistic locking Failure in update manager permissions: "+e.getMessage());
			return Result.makeFail("Company was updated by another operation. Please retry.");
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during update manager permissions: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Unexpected error during update manager permissions: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Map<UserDTO, UserDTO>> hierarchyTree(int companyID, String sessionToken) {
		try {
			logger.info("Verifying session token for company hierarchy for company {}.", companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for viewing company hierarchy for company {}.",companyID);
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to create events.");
				return Result.makeFail("Only signed-in users are allowed to create events. Please use a user account.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			if (!authenticationService.isUserToken(sessionToken)) {
				logger.warn("Only user can access this command",companyID);
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(userID);
			if (user == null) {
				logger.warn("User with ID {0} not found", userID);
				return Result.makeFail("User not found.");
			}
			logger.info("Session token verified successfully.");
			logger.info("Validating user has permissions in ", companyID);
			
			user.validatePermissions(companyID, Owner.class);

			int newID = user.getParentIDForCompany(companyID);
			while(newID != -1) {
				user = userRepository.getUserByID(newID);
				if (user == null) {
					logger.warn("User with ID {0} not found error in hierarchy", newID);
					return Result.makeFail("User not found.");
				}
				newID = user.getParentIDForCompany(companyID);
			}

			user = userRepository.getUserByID(userID);
			if (user == null) {
				logger.warn("User with ID {0} not found error in hierarchy", userID);
				return Result.makeFail("User not found.");
			}
			
			Deque<User> toCheck = new ArrayDeque<>();
			toCheck.push(user);

			Map<UserDTO, UserDTO> result = new TreeMap<>();
			result.put(null, new UserDTO(user));
			while(!toCheck.isEmpty()) {
				User currentUser = toCheck.pop();
				UserDTO currentUserDTO = new UserDTO(currentUser);

				Owner currentRole = (Owner) currentUser.getRole(companyID);
				for (Manager childRole : currentRole.getAssignedManagers()) {
					User childUser = userRepository.getUserByID(childRole.getUserID());
					if (childUser == null) {
						logger.warn("Child user with ID {0} not found in hierarchy for company {1}", childRole.getUserID(), companyID);
						return Result.makeFail("User not found.");
					}

					result.put(currentUserDTO, new UserDTO(childUser));

					if (childUser.getRole(companyID) instanceof Owner) {
						toCheck.push(childUser);
					}
				}
			}
			return Result.makeOk(result);
			
		} catch (IllegalArgumentException | IllegalStateException e) {
			logger.error("Failed to get company hierarchy: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during company hierarchy: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during company hierarchy: " + e.getMessage());
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
