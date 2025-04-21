package com.cloudbalance.controller;

import com.cloudbalance.dto.LoginRequest;
import com.cloudbalance.dto.LoginResponse;
import com.cloudbalance.dto.JwtResponse;
import com.cloudbalance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

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

    // Logout endpoint if necessary
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userService.blacklistRefreshToken(Long.parseLong(token)); // Assuming token holds the user ID
            return ResponseEntity.ok("Logged out successfully.");
        } else {
            return ResponseEntity.badRequest().body("No token provided.");
        }
    }
}
