package com.group16b.DomainLayer.DomainServices;

import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;

public class CompanyHierarchyDomainService {
    private final IUserRepository userRepository;

    public CompanyHierarchyDomainService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //check upwards whether the manager is under the owner in the hierarchy, if we reach the top of the hierarchy without finding the owner, return false
    //works because each user have only 1 direct parent
    private boolean isManagerUnderOwnerTreeTraversal(User manager, User owner, int companyID) {
        if (manager == null || owner == null) {
            return false;
        }
        if(!owner.isOwnerOfCompany(companyID))
        {//save us runtime by checking this before traversing the tree
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
}
