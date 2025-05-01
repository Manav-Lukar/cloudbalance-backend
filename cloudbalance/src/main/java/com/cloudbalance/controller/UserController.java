package com.cloudbalance.controller;

import com.cloudbalance.dto.CreateUserRequest;
import com.cloudbalance.dto.LoginRequest;
import com.cloudbalance.dto.LoginResponse;
import com.cloudbalance.dto.UpdateUserRequest;
import com.cloudbalance.dto.UserResponseDTO;
import com.cloudbalance.service.CloudAccountService;
import com.cloudbalance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/login")
public class UserController {

    private final CloudAccountService cloudAccountService;
    private final UserService userService;

    @Autowired
    public UserController(CloudAccountService cloudAccountService, UserService userService) {
        this.cloudAccountService = cloudAccountService;
        this.userService = userService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        userService.logout(authHeader);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/add-user")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> addUserWithRoleAndAccounts(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        cloudAccountService.addUserWithRoleAndAccounts(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
    }

    @PutMapping("/update-user/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        cloudAccountService.updateUser(userId, request);
        return ResponseEntity.ok("User updated successfully.");
    }
}