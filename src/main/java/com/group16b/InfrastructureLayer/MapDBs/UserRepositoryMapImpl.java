package com.group16b.InfrastructureLayer.MapDBs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;

public class UserRepositoryMapImpl implements IUserRepository {

	private Map<Integer, User> users;
	private Map<String, User> usersByEmail;

	public UserRepositoryMapImpl(Map<Integer, User> users, Map<String, User> usersByEmail) {
		this.users = users;
		this.usersByEmail = usersByEmail;
	}
	public UserRepositoryMapImpl() {
		this.users = new HashMap<>();
	}


	public User getUserByID(int userID) {
		return users.get(userID);
	}


	public void deleteUser(int userID) {
		users.remove(userID);
	}

	public boolean userExists(int userID) {
		return users.containsKey(userID);
	}

	@Override
	public User findByID(String ID) {
		User user = usersByEmail.get(ID);
		if(user == null) {
			return null;
		}
		return (new User(user));

	}

	@Override
	public List<User> getAll() {
		List<User> userList = new java.util.ArrayList<>();
		for(User user : users.values()) {
			userList.add(new User(user));
		}
		return userList;
	}

	@Override
	public void delete(String ID) {
		User user = usersByEmail.get(ID);
		if (user != null) {
			users.remove(user.getUserID());
			usersByEmail.remove(ID);
		}
		

	}

	@Override
	public void save(User user) {
		User existingUser = users.get(user.getUserID());
		if(existingUser != null) {
			long newVersion = user.getVersion();
			long currentVersion = existingUser.getVersion();
			if(newVersion != currentVersion) {
				throw new IllegalStateException("Version mismatch: User has been modified by another process.");
			}
			existingUser.updateUser(user);
			usersByEmail.put(user.getEmail(), existingUser);
			users.put(user.getUserID(), existingUser);
		}
		else{
			user.setVersion(user.getVersion() + 1);
			users.put(user.getUserID(), user);
			usersByEmail.put(user.getEmail(), user);
		}
	}
}
