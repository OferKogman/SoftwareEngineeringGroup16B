package com.group16b.DomainLayer.User;

import java.util.HashMap;
import java.util.Map;

public interface IUserRepository {

	User getUserByEmail(String email);

	void registerUser(User user);

	void addUser(User user);

	void updateUser(User user);

	void deleteUser(String email);

	boolean userExists(String email);
}