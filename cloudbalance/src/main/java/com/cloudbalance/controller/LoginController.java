package com.cloudbalance.controller;

import com.cloudbalance.dto.UserDTO;
import com.cloudbalance.dto.UserResponseDTO;
import com.cloudbalance.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("✅ Welcome to Admin Dashboard");
    }

    @GetMapping("/readonly")
    @PreAuthorize("hasAuthority('READ_ONLY')")
    public ResponseEntity<String> readOnlyDashboard() {
        return ResponseEntity.ok("✅ Welcome to Read-Only Dashboard");
    }

    @GetMapping("/customer")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<String> customerDashboard() {
        return ResponseEntity.ok("✅ Welcome to Customer Dashboard");
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        boolean result = userService.addUser(userDTO);
        if (result) {
            return ResponseEntity.ok("✅ User registered successfully.");
        } else {
            return ResponseEntity.badRequest().body("❌ Email already exists or role not found.");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
