package com.cloudbalance.controller;

import com.cloudbalance.dto.UserDTO;
import com.cloudbalance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    // Accessible only by ADMIN
    @GetMapping("/admin")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("✅ Welcome to Admin Dashboard");
    }

    // Accessible only by READ_ONLY role
    @GetMapping("/readonly")
    public ResponseEntity<String> readOnlyDashboard() {
        return ResponseEntity.ok("✅ Welcome to Read-Only Dashboard");
    }

    // Accessible only by CUSTOMER role
    @GetMapping("/customer")
    public ResponseEntity<String> customerDashboard() {
        return ResponseEntity.ok("✅ Welcome to Customer Dashboard");
    }

    // Public API — user registration
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        userService.createUser(userDTO);
        return ResponseEntity.ok("🎉 User registered successfully!");
    }
}
