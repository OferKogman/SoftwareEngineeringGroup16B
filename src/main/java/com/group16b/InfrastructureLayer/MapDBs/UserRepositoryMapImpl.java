package com.group16b.InfrastructureLayer.MapDBs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.User.User;

public class UserRepositoryMapImpl implements IRepository<User>{

	private Map<String, User> usersByEmail;

	public UserRepositoryMapImpl(Map<Integer, User> users, Map<String, User> usersByEmail) {
		this.usersByEmail = usersByEmail;
	}
	public UserRepositoryMapImpl() {
		this.usersByEmail = new HashMap<>();
	}


	@Override
	public User findByID(String ID) {
		User user = usersByEmail.get(ID);
		if(user == null) {
			throw new IllegalArgumentException("User with email " + ID + " does not exist.");
		}
		return (new User(user));

	}

	@Override
	public List<User> getAll() {
		List<User> userList = new java.util.ArrayList<>();
		for(User user : usersByEmail.values()) {
			userList.add(new User(user));
		}
		return userList;
	}

	@Override
	public void delete(String ID) {
		User user = usersByEmail.get(ID);
		if (user != null) {
			usersByEmail.remove(ID);
		}
	}

	@Override
	public void save(User user) {
		User existingUser = usersByEmail.get(user.getEmail());
		if(existingUser != null) {
			long newVersion = user.getVersion();
			long currentVersion = existingUser.getVersion();
			if(newVersion != currentVersion) {
				throw new IllegalStateException("Version mismatch: User has been modified by another process.");
			}
			user.setVersion(user.getVersion() + 1);
			existingUser.updateUser(user);
			usersByEmail.put(user.getEmail(), existingUser);

		}
		else{
			user.setVersion(user.getVersion() + 1);
			usersByEmail.put(user.getEmail(), user);
		}
	}
}
