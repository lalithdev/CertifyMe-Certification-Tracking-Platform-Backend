package com.certifyme.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.certifyme.app.User;
import com.certifyme.app.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService service;

    // ✅ REGISTER
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return service.register(user);
    }

    // ✅ LOGIN (VERY IMPORTANT)
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        System.out.println("EMAIL: " + user.getEmail());
        System.out.println("PASSWORD: " + user.getPassword());
        return service.login(user.getEmail(), user.getPassword());
    }
    @GetMapping
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }
}