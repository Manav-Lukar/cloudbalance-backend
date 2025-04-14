package com.cloudbalance.controller;

import com.cloudbalance.dto.LoginRequest;
import com.cloudbalance.dto.LoginResponse;
import com.cloudbalance.entity.User;
import com.cloudbalance.repository.UserRepository;
import com.cloudbalance.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("🔐 Login attempt for email: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        logger.error("🚫 User not found for email: {}", loginRequest.getEmail());
                        return new RuntimeException("User not found");
                    });

            String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());

            logger.info("✅ Login successful for user: {}", user.getEmail());
            return ResponseEntity.ok(new LoginResponse(
                    "✅ Login successful",
                    user.getRole().getName(),
                    token,
                    user.getEmail()
            ));
        } catch (BadCredentialsException ex) {
            logger.warn("🚫 Invalid credentials for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body("❌ Invalid email or password");
        } catch (Exception ex) {
            logger.error("🔥 Login error: {}", ex.getMessage());
            return ResponseEntity.internalServerError().body("❌ An error occurred during login.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        logger.info("👋 Logout requested");
        return ResponseEntity.ok("✅ Logged out successfully.");
    }
}
