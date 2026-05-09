package com.group16b.DomainLayer.User.Roles;

import java.util.HashMap;
import java.util.Map;

import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;

public class UserRepositoryImpl implements IUserRepository {

	private static UserRepositoryImpl instance;
	private Map<Integer, User> users = new HashMap<>();

	private UserRepositoryImpl() {
	}

	public static synchronized UserRepositoryImpl getInstance() {
		if (instance == null) {
			instance = new UserRepositoryImpl();
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
