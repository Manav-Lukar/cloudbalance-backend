package com.cloudbalance.controller;

import com.cloudbalance.dto.LoginRequest;
import com.cloudbalance.dto.LoginResponse;
import com.cloudbalance.dto.JwtResponse;
import com.cloudbalance.entity.User;
import com.cloudbalance.repository.UserRepository;
import com.cloudbalance.service.JwtService;
import com.cloudbalance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.authenticateUser(loginRequest);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken) {
        JwtResponse jwtResponse = userService.refreshAccessToken(refreshToken);
        if (jwtResponse != null) {
            return ResponseEntity.ok(jwtResponse);
        } else {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtService.validateRefreshToken(token);
            if (email != null) {
                Optional<User> optionalUser = userRepository.findByEmail(email);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    userService.blacklistRefreshToken(user.getId());
                    return ResponseEntity.ok("Logged out successfully.");
                }
            }
            return ResponseEntity.status(401).body("Invalid token.");
        } else {
            return ResponseEntity.badRequest().body("No token provided.");
        }
    }
}