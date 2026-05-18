package com.group16b.DomainLayer.User;

import java.util.HashMap;
import java.util.Map;

import com.group16b.DomainLayer.Interfaces.IRepository;

public interface IUserRepository extends IRepository<User> {

	boolean userExists(String userEmail);
}