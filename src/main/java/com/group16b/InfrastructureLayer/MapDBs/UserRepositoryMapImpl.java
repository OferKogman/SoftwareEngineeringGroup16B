package com.group16b.InfrastructureLayer.MapDBs;

import java.util.HashMap;
import java.util.Map;

import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;

public class UserRepositoryMapImpl implements IUserRepository {

	private Map<String, User> users = new HashMap<>();
	private Map<String, Long> versionMap;

	public UserRepositoryMapImpl() {
		this.users = new HashMap<>();
		this.versionMap = new HashMap<>();
	}

	public UserRepositoryMapImpl(Map<String, User> users) {
		this.users = users;
		this.versionMap = new HashMap<>();
		users.keySet().forEach(userID -> versionMap.put(userID, 0L));
	}	

	public User getUserByEmail(String email) {
		return users.get(email);
	}

	public void registerUser(User user) {
		users.put(user.getEmail(), user);
	}

	public void addUser(User user) {
		if(user==null) {
			throw new IllegalArgumentException("User cannot be null");
		}
		if(users.containsKey(user.getEmail())) {
			throw new IllegalArgumentException("User with this ID already exists");
		}
		users.put(user.getEmail(), user);
		versionMap.put(user.getEmail(), 0L);
	}

	public void updateUser(User user) {
		if(user==null) {
			throw new IllegalArgumentException("User cannot be null");
		}
		Long currentVersion = versionMap.get(user.getEmail());
		if(currentVersion==null || !userExists(user.getEmail())) {
			throw new IllegalArgumentException("User does not exist");
		}
		if(currentVersion != user.getVersion()) {
			throw new IllegalArgumentException("User has been modified by another process");
		}
		users.put(user.getEmail(), user);
	}

	public void deleteUser(String getEmail) {
		if(!userExists(getEmail)) {
			throw new IllegalArgumentException("User does not exist");
		}
		users.remove(getEmail);
	}

	public boolean userExists(String getEmail) {
		return users.containsKey(getEmail);
	}
}
