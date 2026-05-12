package com.group16b.DomainLayer.DomainServices;

import java.util.Set;

import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.Role;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class CompanyHierarchyDomainService {
    private final IUserRepository userRepository = UserRepositoryMapImpl.getInstance();

    public CompanyHierarchyDomainService() {
    }

    //check upwards whether the manager is under the owner in the hierarchy, if we reach the top of the hierarchy without finding the owner, return false
    //works because each user have only 1 direct parent
    public boolean isManagerUnderOwnerTreeTraversal(User manager, User owner, int companyID) {
        if (manager == null || owner == null) {
            return false;
        }
        if(!owner.isOwnerOfCompany(companyID))
        {//save us runtime by checking this before traversing the tree
            return false;
        }
        if(manager.getUserID()==owner.getUserID())
        {
            return false;
        }
        //traverse upwards in search of owner
        User current = manager;
        while (current != null) {
            Integer parentID = current.getParentIDForCompany(companyID);
            if (parentID == null) {
                return false; // reached top of hierarchy
            }
            if (parentID.equals(owner.getUserID())) {
                return true;
            }
            current = userRepository.getUserByID(parentID);
        }
        return false;
    }

    //will remove a user from a company irarchy
    //PRECONDITIONS: user is not founder of the company
    //this should only be called after ensuring that the callee have high enough clearance
    public void removeUserFromCompany(User user, int companyID)
    {   
        Role userRole = user.getRole(companyID);
        if(userRole instanceof Manager){ 
            int parentID= user.getParentIDForCompany(companyID);
            User parent=userRepository.getUserByID(parentID);
            if (parent != null) {
                Role parentRole = parent.getRole(companyID);
                ((Owner)parentRole).removeManager((Manager)userRole);
            }   
        }
        user.removeRole(companyID);
    }

    /*
    updates manager prems for a company
    preconditons: User have a role is a manager in the company, perms are not empty, perms is not null
    */
    public void updateManagerPermissionsForCompny(User user, int companyID, Set<ManagerPermissions> newPerms)
    {
            Manager manager=(Manager)user.getRole(companyID);
            manager.updatePermissions(newPerms);
    }

    

}