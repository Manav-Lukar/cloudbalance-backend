package com.cloudbalance.controller;

import com.cloudbalance.dto.CreateUserRequest;
import com.cloudbalance.dto.UpdateUserRequest;
import com.cloudbalance.dto.UserResponseDTO;
import com.cloudbalance.service.CloudAccountService;
import com.cloudbalance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/login")
public class UserController {

    @Autowired
    private final CloudAccountService cloudAccountService;
    private final UserService userService;


    @Autowired
    public UserController(CloudAccountService cloudAccountService, UserService userService) {
        this.cloudAccountService = cloudAccountService;
        this.userService = userService;
    }
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @PostMapping("/add-user")
    public ResponseEntity<String> addUserWithRoleAndAccounts(@RequestBody CreateUserRequest request, Authentication authentication) {

            cloudAccountService.addUserWithRoleAndAccounts(request);
            return ResponseEntity.ok("User created successfully.");

    }

    @PutMapping("/update-user/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
            cloudAccountService.updateUser(userId, request);
            return ResponseEntity.ok("User updated successfully.");
    }
}
