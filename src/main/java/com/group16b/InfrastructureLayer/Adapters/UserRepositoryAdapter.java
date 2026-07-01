package com.group16b.InfrastructureLayer.Adapters;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.Database.UserRepository;

@Component
@Primary
public class UserRepositoryAdapter implements IRepository<User> {
    private final UserRepository springRepo;

    public UserRepositoryAdapter(UserRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public List<User> getAll() {
        return springRepo.findAll();
    }

    @Override
    public User findByID(String id) {
        return springRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found."));
    }

    @Override
    public void save(User user) {
        springRepo.save(user);
    }

    @Override
    public void delete(String id) {
        springRepo.deleteById(id);
    }
}
