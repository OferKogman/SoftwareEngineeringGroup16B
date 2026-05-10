package com.group16b.DomainLayer.User.Roles;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Owner extends Manager {
	private final List<Manager> assignedManagers;

	public Owner(Integer parentID) {
		super(parentID,EnumSet.allOf(ManagerPermissions.class), RoleType.OWNER);
		assignedManagers= new ArrayList<>();
	}

	protected Owner(Integer parentID, RoleType role)
	{
		super(parentID, EnumSet.allOf(ManagerPermissions.class),role);
		assignedManagers= new ArrayList<>();
	}


	public List<Manager> getAssignedManagers() {
		ArrayList<Manager> copyList;
		synchronized(lock)
		{
			copyList=new ArrayList<>(assignedManagers);
		}
		return copyList;//we dont want to return the original list to prevent external modification
	}

	//assign a manager to this owner
	public void assignManager(Manager manager) {
		synchronized(lock)
		{
			assignedManagers.add(manager);
		}
	}

	//ahu sharmuta needs lock
	//remove a manager from this owner, if the manager is an owner, move all of its assigned managers to this owner before removing it
	//if the manager isnt a direct asignee of this owner
	public void removeManager(Manager manager) {
		synchronized(lock)
		{	
			if(!assignedManagers.contains(manager))
				return;

			assignedManagers.remove(manager);
			if(manager instanceof Owner){
				//if the manager is an owner, move all of its assigned managers to this owner before removing it
				Owner owner = (Owner) manager;
				int thisID=owner.getAssignerID();
				List<Manager> orphans=owner.getAssignedManagers();
				for(Manager orphan:orphans)
				{
					orphan.setParent(thisID);
					assignedManagers.add(orphan);
				}
			}
		}
	}

}
