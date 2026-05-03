package com.group16b.DomainLayer.User;

import java.util.HashMap;
import java.util.Map;

public class IUserRepository {

	private static IUserRepository instance;
	private Map<Integer, User> users = new HashMap<>();

	private IUserRepository() {
	}

	protected static synchronized IUserRepository getInstance() {
		if (instance == null) {
			instance = new IUserRepository();
		}
		return instance;
	}

	protected User getUserByID(int userID) {
		return users.get(userID);
	}

	protected void addUser(User user) {
		users.put(user.getUserID(), user);
	}

	protected void updateUser(User user) {
		users.put(user.getUserID(), user);
	}

	protected void deleteUser(int userID) {
		users.remove(userID);
	}

	protected boolean userExists(int userID) {
		return users.containsKey(userID);
	}
}