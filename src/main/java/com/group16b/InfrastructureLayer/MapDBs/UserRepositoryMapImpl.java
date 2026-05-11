package com.group16b.InfrastructureLayer.MapDBs;

import java.util.HashMap;
import java.util.Map;

import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;

public class UserRepositoryMapImpl implements IUserRepository {

	private static UserRepositoryMapImpl instance;
	private Map<Integer, User> users = new HashMap<>();

	private UserRepositoryMapImpl() {
	}

	public static synchronized UserRepositoryMapImpl getInstance() {
		if (instance == null) {
			instance = new UserRepositoryMapImpl();
		}
		return instance;
	}

	public User getUserByID(int userID) {
		return users.get(userID);
	}

	public void registerUser(User user) {
		users.put(user.getUserID(), user);
	}

	public void addUser(User user) {
		users.put(user.getUserID(), user);
	}

	public void updateUser(User user) {
		users.put(user.getUserID(), user);
	}

	public void deleteUser(int userID) {
		users.remove(userID);
	}

	public boolean userExists(int userID) {
		return users.containsKey(userID);
	}
}
