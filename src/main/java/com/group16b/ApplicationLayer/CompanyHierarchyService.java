package com.group16b.ApplicationLayer;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group16b.ApplicationLayer.DTOs.HierarchyNodeDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.HierarchyNodeData;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.RequestContext;
import com.group16b.InfrastructureLayer.Security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import com.group16b.ApplicationLayer.Interfaces.IBusinessNotificationService;

import io.jsonwebtoken.JwtException;

@Service
@Transactional
public class CompanyHierarchyService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final IAuthenticationService authenticationService;
	private final IRepository<User> userRepository;
	private final IProductionCompanyRepository productionCompanyRepository;
	private IBusinessNotificationService businessNotifications;

	public CompanyHierarchyService(IAuthenticationService authenticationService,
			IProductionCompanyRepository productionCompanyRepository, IRepository<User> userRepository) {
		this.authenticationService = authenticationService;
		this.productionCompanyRepository = productionCompanyRepository;
		this.userRepository = userRepository;
	}

	public Result<Boolean> assignOwnerToCompany(int companyID, String targetID, String sessionToken) {
		while (true) {
			try {
				// auth
				logger.info(
						"CompanyHierarchyService.assignOwnerToCompany: Verifying session token for owner assignment of user {} to company {}.",
						targetID, companyID);
				String userID = validateAndGetUserID(sessionToken);
				userRepository.findByID(userID);
				logger.info("CompanyHierarchyService.assignOwnerToCompany: Session token verified successfully.");

				logger.info("CompanyHierarchyService.assignOwnerToCompany: attempting to retrieve target User {}",
						targetID);
				userRepository.findByID(targetID);

				logger.info(
						"CompanyHierarchyService.assignOwnerToCompany: attempting to retrieve production company {}",
						companyID);
				ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

				logger.info(
						"CompanyHierarchyService.assignOwnerToCompany: Attempting to send owner invite to user {} by user {} in company {}",
						targetID, userID, companyID);
				company.AssignOwner(userID, targetID);
				logger.info(
						"CompanyHierarchyService.assignOwnerToCompany: user {} Succefully invited target {} to be owner in company {}",
						userID, targetID, companyID);

				logger.info(
						"CompanyHierarchyService.assignOwnerToCompany: attempting to save changed in production company {}",
						companyID);
				productionCompanyRepository.save(company);
				logger.info("CompanyHierarchyService.assignOwnerToCompany: Successfuly saved the {} company",
						companyID);

				notifyOwnerInvite(targetID, companyID, company.getName(), userID);

				return Result.makeOk(true);
			} catch (IllegalArgumentException e) {
				logger.warn("CompanyHierarchyService.assignOwnerToCompany: Runtime error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (AuthException e) {
				logger.warn("CompanyHierarchyService.assignOwnerToCompany: Auth error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (OptimisticLockingFailureException e) {
				logger.warn(
						"CompanyHierarchyService.assignOwnerToCompany: Optimistic locking Failure: " + e.getMessage());
				continue;
			} catch (JwtException e) {
				logger.error(
						"CompanyHierarchyService.assignOwnerToCompany: JWT authentication error: " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
			} catch (Exception e) {
				logger.error("CompanyHierarchyService.assignOwnerToCompany: Unexpected error: " + e.getMessage());
				return Result.makeFail("An unexpected error occurred: " + e.getMessage());
			}
		}
	}

	public Result<Boolean> assignManagerToCompany(int companyID, String targetID, Set<ManagerPermissions> permissions,
			String sessionToken) {
		while (true) {
			try {
				// auth
				logger.info(
						"CompanyHierarchyService.assignManagerToCompany: Verifying session token for manager assignment of user {} to company {}.",
						targetID, companyID);
				String userID = validateAndGetUserID(sessionToken);
				logger.info("CompanyHierarchyService.assignManagerToCompany: Session token verified successfully.");

				logger.info("CompanyHierarchyService.assignManagerToCompany: attempting to retrieve target User {}",
						targetID);
				userRepository.findByID(targetID);

				logger.info(
						"CompanyHierarchyService.assignManagerToCompany: attempting to retrieve production company {}",
						companyID);
				ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

				logger.info(
						"CompanyHierarchyService.assignManagerToCompany: Attempting to send manager invite to user {} by user {} in company {}",
						targetID, userID, companyID);
				company.AssignManager(userID, targetID, permissions);
				logger.info(
						"CompanyHierarchyService.assignManagerToCompany: user {} Succefully invited target {} to be manager in company {}",
						userID, targetID, companyID);

				logger.info(
						"CompanyHierarchyService.assignManagerToCompany: attempting to save changed in production company {}",
						companyID);
				productionCompanyRepository.save(company);
				logger.info("CompanyHierarchyService.assignManagerToCompany: succesfuly save company {}", companyID);

				return Result.makeOk(true);
			} catch (IllegalArgumentException e) {
				logger.warn("CompanyHierarchyService.assignManagerToCompany: Runtime error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (AuthException e) {
				logger.warn("CompanyHierarchyService.assignManagerToCompany: Auth error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (OptimisticLockingFailureException e) {
				logger.warn("CompanyHierarchyService.assignManagerToCompany: Optimistic locking Failure: "
						+ e.getMessage());
				continue;
			} catch (JwtException e) {
				logger.error(
						"CompanyHierarchyService.assignManagerToCompany: JWT authentication error: " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
			} catch (Exception e) {
				logger.error("CompanyHierarchyService.assignManagerToCompany: Unexpected error: " + e.getMessage());
				return Result.makeFail("An unexpected error occurred: " + e.getMessage());
			}
		}
	}

	public Result<Boolean> acceptInviteToCompany(int companyID, String assignerID, String sessionToken) {
		while (true) {
			try {
				// auth
				logger.info(
						"CompanyHierarchyService.acceptInviteToCompany: Verifying session token for accepting invite assignment to company {} by assigner {}.",
						companyID, assignerID);
				String userID = validateAndGetUserID(sessionToken);
				logger.info("CompanyHierarchyService.acceptInviteToCompany: Session token verified successfully.");

				logger.info(
						"CompanyHierarchyService.acceptInviteToCompany: ensuring assigner {} exists for accept invite",
						assignerID);
				userRepository.findByID(assignerID);

				logger.info(
						"CompanyHierarchyService.acceptInviteToCompany: trying to retrieve company {} for accept invite",
						companyID);
				ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

				logger.info(
						"CompanyHierarchyService.acceptInviteToCompany: trying to accept invite for user {} assigner by {} in company {}",
						userID, assignerID, companyID);
				company.acceptInvite(userID, assignerID);

				logger.info(
						"CompanyHierarchyService.acceptInviteToCompany: user {} have succesfully accepted an invite to company {} by assigner {}",
						userID, companyID, assignerID);

				logger.info(
						"CompanyHierarchyService.acceptInviteToCompany: Trying to save change for accepting invite");
				productionCompanyRepository.save(company);
				logger.info(
						"CompanyHierarchyService.acceptInviteToCompany: Succesfully saved company {} after accepting invite",
						companyID);

				notifyInviteAccepted(assignerID, userID, companyID, company.getName());

				return Result.makeOk(true);
			} catch (IllegalArgumentException e) {
				logger.warn("CompanyHierarchyService.acceptInviteToCompany: Runtime error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (AuthException e) {
				logger.warn("CompanyHierarchyService.acceptInviteToCompany: Auth error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (OptimisticLockingFailureException e) {
				logger.warn(
						"CompanyHierarchyService.acceptInviteToCompany: Optimistic locking Failure: " + e.getMessage());
				continue;
			} catch (JwtException e) {
				logger.error(
						"CompanyHierarchyService.acceptInviteToCompany: JWT authentication error: " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
			} catch (Exception e) {
				logger.error(
						"CompanyHierarchyService.acceptInviteToCompany: Unexpected error during: " + e.getMessage());
				return Result.makeFail("An unexpected error occurred: " + e.getMessage());
			}
		}
	}

	public Result<Boolean> rejectInviteToCompany(int companyID, String assignerID, String sessionToken) {

		while (true) {
			try {
				// auth
				logger.info(
						"CompanyHierarchyService.rejectInviteToCompany: Verifying session token for rejection invite assignment to company {} by assigner {}.",
						companyID, assignerID);
				String userID = validateAndGetUserID(sessionToken);

				logger.info("CompanyHierarchyService.rejectInviteToCompany: Session token verified successfully.");

				logger.info(
						"CompanyHierarchyService.rejectInviteToCompany: ensuring assigner {} exists for reject invite",
						assignerID);
				userRepository.findByID(assignerID);

				logger.info(
						"CompanyHierarchyService.rejectInviteToCompany: trying to retrieve company {} for reject invite",
						companyID);
				ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

				logger.info(
						"CompanyHierarchyService.rejectInviteToCompany: trying to reject invite for user {} assigner by {} in company {}",
						userID, assignerID, companyID);
				company.rejectInvite(userID, assignerID);

				logger.info(
						"CompanyHierarchyService.rejectInviteToCompany: user {} have succesfully rejected an invite to company {} by assigner {}",
						userID, companyID, assignerID);

				logger.info("CompanyHierarchyService.rejectInviteToCompany: Trying to save change for reject invite");
				productionCompanyRepository.save(company);
				logger.info(
						"CompanyHierarchyService.rejectInviteToCompany: Succesfully saved company {} after rejected invite",
						companyID);

				notifyInviteRejected(assignerID, userID, companyID, company.getName());

				return Result.makeOk(true);
			} catch (IllegalArgumentException e) {
				logger.warn("CompanyHierarchyService.rejectInviteToCompany: Runtime error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (AuthException e) {
				logger.warn("CompanyHierarchyService.rejectInviteToCompany: Auth error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (OptimisticLockingFailureException e) {
				logger.warn(
						"CompanyHierarchyService.rejectInviteToCompany: Optimistic locking Failure: " + e.getMessage());
				continue;
			} catch (JwtException e) {
				logger.error(
						"CompanyHierarchyService.rejectInviteToCompany: JWT authentication error: " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
			} catch (Exception e) {
				logger.error("CompanyHierarchyService.rejectInviteToCompany: Unexpected error: " + e.getMessage());
				return Result.makeFail("An unexpected error occurred: " + e.getMessage());
			}
		}
	}

	public Result<Boolean> forfeitOwnership(int companyID, String sessionToken) {
		while (true) {
			try {
				// auth
				logger.info(
						"CompanyHierarchyService.forfeitOwnership: Verifying session token for Forfeit ownership in company {}.",
						companyID);
				String userID = validateAndGetUserID(sessionToken);
				logger.info("CompanyHierarchyService.forfeitOwnership: Session token verified successfully.");

				logger.info(
						"CompanyHierarchyService.forfeitOwnership: trying to retrieve company {} for Forfeit ownership by user {}",
						companyID, userID);
				ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

				logger.info(
						"CompanyHierarchyService.forfeitOwnership: trying to Forfeit ownership by user{} in company {}",
						userID, companyID);
				company.forfeitOwnership(userID);

				logger.info(
						"CompanyHierarchyService.forfeitOwnership: user {} have succesfully Forfeited ownership in company {}.",
						userID, companyID);

				logger.info("CompanyHierarchyService.forfeitOwnership: Trying to save change for Forfeit ownership");
				productionCompanyRepository.save(company);
				logger.info(
						"CompanyHierarchyService.forfeitOwnership: Succesfully saved company {} after Forfeiting ownership",
						companyID);
				return Result.makeOk(true);
			} catch (IllegalArgumentException e) {
				logger.warn("CompanyHierarchyService.forfeitOwnership: Runtime error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (AuthException e) {
				logger.warn("CompanyHierarchyService.forfeitOwnership: Auth error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (OptimisticLockingFailureException e) {
				logger.warn("CompanyHierarchyService.forfeitOwnership: Optimistic locking Failure: " + e.getMessage());
				continue;
			} catch (JwtException e) {
				logger.error("CompanyHierarchyService.forfeitOwnership: JWT authentication error: " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
			} catch (Exception e) {
				logger.error("CompanyHierarchyService.forfeitOwnership: Unexpected error: " + e.getMessage());
				return Result.makeFail("An unexpected error occurred: " + e.getMessage());
			}
		}
	}

	public Result<Boolean> removeOwnerManager(String targetID, int companyID, String sessionToken) {
		while (true) {
			try {
				// auth
				logger.info(
						"CompanyHierarchyService.removeOwnerManager: Verifying session token for remove membership in company {}.",
						companyID);
				String userID = validateAndGetUserID(sessionToken);
				logger.info("CompanyHierarchyService.removeOwnerManager: Session token verified successfully.");

				logger.info("CompanyHierarchyService.removeOwnerManager: ensuring target user {} exists", targetID);
				userRepository.findByID(targetID);

				logger.info(
						"CompanyHierarchyService.removeOwnerManager: trying to retrieve company {} for remove membership of target {} by user {}",
						companyID, targetID, userID);
				ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

				logger.info(
						"CompanyHierarchyService.removeOwnerManager: trying to remove membership of target {} by user{} in company {}",
						targetID, userID, companyID);
				company.removeMemberByOwner(userID, targetID);

				logger.info(
						"CompanyHierarchyService.removeOwnerManager: user {} have succesfully remove membership of target {} in company {}.",
						userID, targetID, companyID);

				logger.info(
						"CompanyHierarchyService.removeOwnerManager: Trying to save change for remove membership of target {}",
						targetID);
				productionCompanyRepository.save(company);
				logger.info(
						"CompanyHierarchyService.removeOwnerManager: Succesfully saved company {} after remove membership of target {}",
						companyID, targetID);

				notifyMembershipRemoved(targetID, companyID, company.getName());

				return Result.makeOk(true);
			} catch (IllegalArgumentException e) {
				logger.warn("CompanyHierarchyService.removeOwnerManager: Runtime error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (AuthException e) {
				logger.warn("CompanyHierarchyService.removeOwnerManager: Authentication error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (OptimisticLockingFailureException e) {
				logger.warn(
						"CompanyHierarchyService.removeOwnerManager: Optimistic locking Failure: " + e.getMessage());
				continue;
			} catch (JwtException e) {
				logger.error("CompanyHierarchyService.removeOwnerManager: JWT authentication error: " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
			} catch (Exception e) {
				logger.error("CompanyHierarchyService.removeOwnerManager: Unexpected error: " + e.getMessage());
				return Result.makeFail("An unexpected error occurred: " + e.getMessage());
			}
		}
	}

	public Result<Boolean> changeManagerPermission(String targetID, int companyID,
			Set<ManagerPermissions> newPermissions, String sessionToken) {
		while (true) {
			try {
				// auth
				logger.info(
						"CompanyHierarchyService.changeManagerPermission: Verifying session token for update manager permissions of target {} in company {}.",
						targetID, companyID);
				String userID = validateAndGetUserID(sessionToken);
				logger.info("CompanyHierarchyService.changeManagerPermission: Session token verified successfully.");

				logger.info("CompanyHierarchyService.changeManagerPermission: ensuring target user {} exists",
						targetID);
				userRepository.findByID(targetID);

				logger.info(
						"CompanyHierarchyService.changeManagerPermission: trying to retrieve company {} for update manager permissions of target {} by user {}",
						companyID, targetID, userID);
				ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

				logger.info(
						"CompanyHierarchyService.changeManagerPermission: trying to update manager permissions of target {} by user{} in company {}",
						targetID, userID, companyID);
				company.updatePermissionsOfManager(userID, targetID, newPermissions);

				logger.info(
						"CompanyHierarchyService.changeManagerPermission: user {} have succesfully updated manager permissions of target {} in company {}.",
						userID, targetID, companyID);

				logger.info(
						"CompanyHierarchyService.changeManagerPermission: Trying to save change for update manager permissions of target {}",
						targetID);
				productionCompanyRepository.save(company);
				logger.info(
						"CompanyHierarchyService.changeManagerPermission: Succesfully saved company {} after update manager permissions of target {} by user {}",
						companyID, targetID, userID);

				notifyPermissionsChanged(targetID, companyID, company.getName());

				return Result.makeOk(true);
			} catch (IllegalArgumentException e) {
				logger.warn("CompanyHierarchyService.changeManagerPermission: Runtime error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (AuthException e) {
				logger.warn("CompanyHierarchyService.changeManagerPermission: Authentication error: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			} catch (OptimisticLockingFailureException e) {
				logger.warn("CompanyHierarchyService.changeManagerPermission: Optimistic locking Failure: "
						+ e.getMessage());
				continue;
			} catch (JwtException e) {
				logger.error(
						"CompanyHierarchyService.changeManagerPermission: JWT authentication error: " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
			} catch (Exception e) {
				logger.error("CompanyHierarchyService.changeManagerPermission: Unexpected error: " + e.getMessage());
				return Result.makeFail("An unexpected error occurred: " + e.getMessage());
			}
		}
	}

	public Result<List<HierarchyNodeDTO>> hierarchyTree(int companyID, String sessionToken) {
		try {
			// auth
			logger.info(
					"CompanyHierarchyService.hierarchyTree: Verifying session token for get hierarchy tree for company {}",
					companyID);
			String userID = validateAndGetUserID(sessionToken);
			logger.info("CompanyHierarchyService.hierarchyTree: Session token verified successfully.");

			logger.info(
					"CompanyHierarchyService.hierarchyTree: trying to retrieve company {} for retrieve hierarchy tree by user {}",
					companyID, userID);
			ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

			logger.info("CompanyHierarchyService.hierarchyTree: trying to get hierarchy tree of company {} by user {}",
					companyID, userID);
			List<HierarchyNodeData> hierarchy = company.getHierarchyTree(userID);

			List<HierarchyNodeDTO> dtoResult = hierarchy.stream().map(HierarchyNodeDTO::new).toList();

			logger.info(
					"CompanyHierarchyService.hierarchyTree: user {} have succesfully retrieved the hierarchy tree for ccompany {}",
					userID, companyID);

			return Result.makeOk(dtoResult);
		} catch (IllegalArgumentException e) {
			logger.warn("CompanyHierarchyService.hierarchyTree: Runtime error: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (AuthException e) {
			logger.warn("CompanyHierarchyService.hierarchyTree: Auth error: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (Exception e) {
			logger.error("CompanyHierarchyService.hierarchyTree: Unexpected error: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Set<ManagerPermissions>> getComapanyPermissions(int companyID)
    {
        try{
            logger.info("companyHierarchyService.getComapanyPermissions: retrieving permissions");

            String userId=validateRoleAndGetUserId();
            ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(companyID));

            logger.info("companyHierarchyService.getComapanyPermissions: succesfully retrieved permissions for user {} in company {}",userId,companyID);
            return Result.makeOk(company.getUserPermissions(userId));

        } catch(AuthException e){
            logger.warn("companyHierarchyService.getComapanyPermissions: AuthException: {}",e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalArgumentException e){
            logger.warn("companyHierarchyService.getComapanyPermissions: IllegalArgumentException: {}",e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e){
            logger.error("companyHierarchyService.getComapanyPermissions: Unexpected Exception: ",e);
            return Result.makeFail("An unexpected error occured, pls try again later.");
        }
    }

	public Result<Boolean> isOwner(int companyId)
	{
		try
		{
			logger.info("companyHierarchyService.isOwner: verifying ownership for company {}",companyId);
			String userID= validateRoleAndGetUserId();
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(companyId));
			logger.info("companyHierarchyService.isOwner: verifying ownership of user {} for company {}",userID,companyId);
			if(!company.isOwner(userID))
			{
				logger.info("companyHierarchyService.isOwner: user {} is not owner for company {}",userID,companyId);
				return Result.makeOk(false);
			}
			logger.info("companyHierarchyService.isOwner: user {} is owner for company {}",userID,companyId);
			return Result.makeOk(true);
		} catch (AuthException e){
			logger.info("companyHierarchyService.isOwner: AuthException: ",e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalArgumentException e){
			logger.info("companyHierarchyService.isOwner: IllegalArgumentException: ",e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch(Exception e){
			logger.error("companyHierarchyService.isOwner: Unexpected Exception: ",e);
			return Result.makeFail("An unexpected error occured, pls try again later.");
		}
	}

	private String validateAndGetUserID(String sessionToken) {
		if (!authenticationService.validateToken(sessionToken)) {
			throw new AuthException("Invalid Token");
		}
		if (!authenticationService.isUserToken(sessionToken)) {
			throw new AuthException("Only users are allowed to perform operation");
		}
		String userID = authenticationService.extractSubjectFromToken(sessionToken);
		// verify user exists in the database, i.e not a stale user
		userRepository.findByID(userID);
		return userID;
	}

	private String validateRoleAndGetUserId()
    {
        //if we do implement error 403 then this if will also disapear, along with the function
        if(!Role.SIGNED.equals(RequestContext.getRole()))
            throw new AuthException("Only users are allowed to perform this operation.");

        return RequestContext.getUserId();
    }

	@Autowired(required = false)
	public void setBusinessNotifications(IBusinessNotificationService businessNotifications) {
		this.businessNotifications = businessNotifications;
	}

	private void notifyOwnerInvite(String targetID, int companyID, String companyName, String inviterID) {
		if (businessNotifications != null) {
			businessNotifications.companyOwnerInvite(targetID, companyID, companyName, inviterID);
		}
	}

	private void notifyManagerInvite(String targetID, int companyID, String companyName, String inviterID) {
		if (businessNotifications != null) {
			businessNotifications.companyManagerInvite(targetID, companyID, companyName, inviterID);
		}
	}

	private void notifyInviteAccepted(String assignerID, String targetID, int companyID, String companyName) {
		if (businessNotifications != null) {
			businessNotifications.companyInviteAccepted(assignerID, targetID, companyID, companyName);
		}
	}

	private void notifyInviteRejected(String assignerID, String targetID, int companyID, String companyName) {
		if (businessNotifications != null) {
			businessNotifications.companyInviteRejected(assignerID, targetID, companyID, companyName);
		}
	}

	private void notifyMembershipRemoved(String targetID, int companyID, String companyName) {
		if (businessNotifications != null) {
			businessNotifications.companyMembershipRemoved(targetID, companyID, companyName);
		}
	}

	private void notifyPermissionsChanged(String targetID, int companyID, String companyName) {
		if (businessNotifications != null) {
			businessNotifications.companyPermissionsChanged(targetID, companyID, companyName);
		}
	}

}
