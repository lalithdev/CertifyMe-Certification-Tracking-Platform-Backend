package com.certifyme.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import com.certifyme.app.User;
import com.certifyme.app.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository repo;

    public User register(User user) {
        if (user.getRole() == null) {
            user.setRole("STUDENT"); // ✅
        }
        return repo.save(user);
    }
    public User login(String email, String password) {

        System.out.println("LOGIN ATTEMPT: " + email);

        User user = repo.findByEmail(email);

        // 🔥 VERY IMPORTANT (PREVENT CRASH)
        if (user == null) {
            System.out.println("User NOT FOUND");
            throw new RuntimeException("User not found");
        }

        System.out.println("User FOUND: " + user.getEmail());

        if (!user.getPassword().equals(password)) {
            System.out.println("Wrong password");
            throw new RuntimeException("Wrong password");
        }

        return user;
    }
    
    public List<User> getAllUsers() {
        return repo.findAll();
    }
}