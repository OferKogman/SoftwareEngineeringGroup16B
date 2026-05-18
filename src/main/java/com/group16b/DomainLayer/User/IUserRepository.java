package com.group16b.DomainLayer.User;

import java.util.HashMap;
import java.util.Map;

import com.group16b.DomainLayer.Interfaces.IRepository;

public interface IUserRepository extends IRepository<User> {

	User getUserByID(int userID);

	void deleteUser(int userID);

	boolean userExists(int userID);
}