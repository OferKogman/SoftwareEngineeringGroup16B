package com.group16b.DomainLayer.User;

import java.util.HashMap;
import java.util.Map;

public interface IUserRepository {

	User getUserByID(int userID);

	void registerUser(User user);

	void addUser(User user);

	void updateUser(User user);

	void deleteUser(int userID);

	boolean userExists(int userID);
}