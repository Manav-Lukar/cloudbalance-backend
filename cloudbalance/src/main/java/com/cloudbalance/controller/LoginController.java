package com.cloudbalance.controller;

import com.cloudbalance.dto.LoginRequest;
import com.cloudbalance.dto.UserDTO;
import com.cloudbalance.dto.UserResponseDTO;
import com.cloudbalance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/login")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminDashboard() {
        logger.info("🏛️ Accessed admin dashboard");
        return ResponseEntity.ok("✅ Welcome to Admin Dashboard");
    }

    @GetMapping("/readonly")
    public ResponseEntity<String> readOnlyDashboard() {
        logger.info("📖 Accessed read-only dashboard");
        return ResponseEntity.ok("✅ Welcome to Read-Only Dashboard");
    }

    @GetMapping("/customer")
    public ResponseEntity<String> customerDashboard() {
        logger.info("🛒 Accessed customer dashboard");
        return ResponseEntity.ok("✅ Welcome to Customer Dashboard");
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        logger.info("📝 Registering new user: {}", userDTO.getEmail());
        boolean result = userService.addUser(userDTO);
        if (result) {
            logger.info("✅ User registered successfully: {}", userDTO.getEmail());
            return ResponseEntity.ok("✅ User registered successfully.");
        } else {
            logger.warn("🚫 Registration failed — Email exists or role missing: {}", userDTO.getEmail());
            return ResponseEntity.badRequest().body("❌ Email already exists or role not found.");
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody LoginRequest LoginRequest) {
        logger.info("🔐 Authenticating user: {}", LoginRequest.getEmail());
        boolean authenticated = userService.authenticateUser(LoginRequest.getEmail(), LoginRequest.getPassword());
        if (authenticated) {
            logger.info("✅ Authentication successful: {}", LoginRequest.getEmail());
            return ResponseEntity.ok("✅ Login successful & last login time updated.");
        } else {
            logger.warn("🚫 Invalid credentials for: {}", LoginRequest.getEmail());
            return ResponseEntity.status(401).body("❌ Invalid email or password.");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        logger.info("📋 Fetching all users");
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
