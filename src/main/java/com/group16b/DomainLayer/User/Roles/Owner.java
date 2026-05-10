package com.group16b.DomainLayer.User.Roles;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Owner extends Manager {
	private final List<Manager> assignedManagers;

	public Owner(Integer parentID) {
		super(parentID,EnumSet.allOf(ManagerPermissions.class));
		this.roleType = RoleType.OWNER;
		assignedManagers= new ArrayList<>();
	}

	public List<Manager> getAssignedManagers() {
		return new ArrayList<>(assignedManagers);//we dont want to return the original list to prevent external modification
	}

	//assign a manager to this owner
	public void assignManager(Manager manager) {
		assignedManagers.add(manager);
	}

	//remove a manager from this owner, if the manager is an owner, move all of its assigned managers to this owner before removing it
	//if the manager isnt a direct asignee of this owner
	public void removeManager(Manager manager) {
		assignedManagers.remove(manager);
		if(manager instanceof Owner){
			//if the manager is an owner, move all of its assigned managers to this owner before removing it
			Owner owner = (Owner) manager;
			assignedManagers.addAll(owner.getAssignedManagers());
			owner.getAssignedManagers().clear();
		}
	}

}
