package com.group16b.InfrastructureLayer.MapDBs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.User.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryMapImpl implements IRepository<User> {

	private Map<String, User> users;

	public UserRepositoryMapImpl() {
		users = new HashMap<>();
	}
	public UserRepositoryMapImpl(Map<String, User> users) {
		this.users = users;
	}


	@Override
	public User findByID(String ID) {
		User user = users.get(ID);
		if (user == null) {
			throw new IllegalArgumentException("User with ID " + ID + " not found.");
		}
		return new User(user);
	}

	@Override
	public List<User> getAll() {
		List<User> userList = new java.util.ArrayList<>();
		for (User user : users.values()) {
			userList.add(new User(user));
		}
		return userList;
	}

	@Override
	public synchronized void delete(String ID) {
		if (!users.containsKey(ID)) {
			throw new IllegalArgumentException("User with ID " + ID + " not found.");
		}
		users.remove(ID);
	}

	@Override
	public synchronized void save(User newUser) {
		User existingUser = users.get(newUser.getEmail());
		if(existingUser != null){
			long newVersion = existingUser.getVersion();
			long currentVersion = newUser.getVersion();
			if(newVersion != currentVersion) {
				throw new IllegalStateException("User has been modified by another process. Please reload and try again.");
			}
			existingUser.update(newUser);
			users.put(existingUser.getEmail(), existingUser);
		}
		else{
			newUser.setVersion(newUser.getVersion() + 1);
			users.put(newUser.getEmail(), newUser);
		}

	}
}
