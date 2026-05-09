package com.group16b.ApplicationLayer;

import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.Role;
import com.group16b.DomainLayer.User.Roles.UserRepositoryImpl;

import io.jsonwebtoken.JwtException;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class UserService {

	private IUserRepository userRepository;
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	private final IAuthenticationService authenticationService;
	public UserService(IAuthenticationService authenticationService, IUserRepository userRepository) {
		this.userRepository = userRepository;
		this.authenticationService = authenticationService;
	}

	public void registerUser(String email, String password) {
		User newUser = new User(email, password);
		userRepository.addUser(newUser);
	}

	public void updateUserPassword(int userID, String oldPassword, String newPassword) {
		User user = userRepository.getUserByID(userID);
		if (user == null) {
			System.out.println("User not found.");
			return;
		}
		if (!user.confirmPassword(oldPassword)) {
			System.out.println("Old password is incorrect.");
			return;
		}
		if (!user.confirmPassword(newPassword)) {
			System.out.println("New password cannot be the same as the old password.");
			return;
		} // else, user is not null and old password is correct and new password is
			// different from old password
		user.setPassword(newPassword);
		userRepository.updateUser(user);

	}

	public boolean authenticateUser(int userID, String password) {
		User user = userRepository.getUserByID(userID);
		if (user != null) {
			return user.confirmPassword(password);
		}
		return false;
	}

	public void deleteUser(int userID) {
		userRepository.deleteUser(userID);
	}

	public boolean userExists(int userID) {
		return userRepository.userExists(userID);
	}

	public Result<Boolean> assignOwnerToCompany(int userID, int companyID, int targetID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for Owner assignment of user {0} to company {1} by user {2}.", targetID, companyID, userID);
			if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for Owner assignment of user {0} to company {1} by user {2}.", targetID, companyID, userID);
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
			logger.info("Session token verified successfully.");

			//get perms
			logger.info("Validating user permissions for owner assignment.");
			user.validatePermissions(companyID, Owner.class);
			logger.info("User permissions validated successfully.");

			//get target user
			logger.info("retrieving target user for Owner assignment.");
			User targetUser = userRepository.getUserByID(targetID);
			if (targetUser==null) {
				logger.warn("Target user with ID {0} not found for Owner assignment.", targetID);
				return Result.makeFail("Target user not found.");
			}

			//ensure not owner already
			logger.info("ensuring target isnt already an owner for company.");
			targetUser.getUserInvitesLock().lock();
			try {
				//add invite to target user
				logger.info("Adding owner assignment invite to target user.");
				targetUser.addInvite(companyID, userID, new Owner(userID));
			}
			catch (IllegalArgumentException e) {
				logger.error("Failed to add owner assignment invite: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
			finally {
				targetUser.getUserInvitesLock().unlock();
			}
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to deactivate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> acceptInviteToCompany(int userID, int companyID, int assignerID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for accepting invite assignment to company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
			if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for accepting invite assignment to company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
			logger.info("Session token verified successfully.");
			if(!userRepository.userExists(assignerID))
			{
				logger.warn("Assigner user with ID {0} not found for accepting invite assignment to company {1} by user {2}.", assignerID, companyID, userID);
				return Result.makeFail("Assigner user not found.");
			}

			//check that invite exists and accept it
			logger.info("accepting invite assignment invite for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
			user.getUserInvitesLock().lock();
			try {
				user.acceptInvite(companyID, assignerID);
				logger.info("Invite assignment invite accepted successfully for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
			} 
			catch (IllegalArgumentException e) {
				logger.error("Failed to add invite assignment invite: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
			finally {
				user.getUserInvitesLock().unlock();
			}

			return Result.makeOk(true);
		} catch (IllegalArgumentException e) {
			logger.error("Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to deactivate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Boolean> assignManagerToCompany(int userID, int companyID, int targetID, Set<ManagerPermissions> permissions, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for Manager assignment of user {0} to company {1} by user {2}.", targetID, companyID, userID);
			if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for Manager assignment of user {0} to company {1} by user {2}.", targetID, companyID, userID);
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
			logger.info("Session token verified successfully.");

			//get perms
			logger.info("Validating user permissions for manager assignment.");
			user.validatePermissions(companyID, Owner.class);
			logger.info("User permissions validated successfully.");

			//get target user
			logger.info("retrieving target user for Manager assignment.");
			User targetUser = userRepository.getUserByID(targetID);
			if (targetUser==null) {
				logger.warn("Target user with ID {0} not found for Manager assignment.", targetID);
				return Result.makeFail("Target user not found.");
			}

			//send invite
			logger.info("ensuring target isnt already an owner for company.");
			targetUser.getUserInvitesLock().lock();
			try {
				logger.info("Adding manager assignment invite to target user.");
				targetUser.addInvite(companyID, userID, new Manager(userID, permissions));
			}
			catch (IllegalArgumentException e) {
				logger.error("Failed to add manager assignment invite: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
			finally {
				targetUser.getUserInvitesLock().unlock();
			}
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to deactivate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

}