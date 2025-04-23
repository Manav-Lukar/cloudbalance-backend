package com.cloudbalance.controller;

import com.cloudbalance.dto.AssignRequest;
import com.cloudbalance.dto.CloudAccountsDto;
import com.cloudbalance.dto.CreateUserRequest;
import com.cloudbalance.dto.UpdateUserRequest;
import com.cloudbalance.entity.CloudAccount;
import com.cloudbalance.entity.User;
import com.cloudbalance.service.CloudAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")

@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER)")

public class CloudAccountAdminController {

    @Autowired
    private CloudAccountService cloudAccountService;

    @GetMapping("/cloud-accounts")
    public ResponseEntity<List<CloudAccountsDto>> getAllAccounts() {
        return ResponseEntity.ok(cloudAccountService.getAllCloudAccountsDto());
    }

    @GetMapping("/cloud-accounts/orphan")
    public ResponseEntity<List<CloudAccount>> getOrphanAccounts() {
        return ResponseEntity.ok(cloudAccountService.getOrphanAccounts());
    }

    // Add Cloud Account
    @PostMapping("/add-cloud-accounts")
    public ResponseEntity<String> addCloudAccount(@RequestBody CloudAccountsDto dto) {
        cloudAccountService.addCloudAccount(dto);
        return ResponseEntity.ok("Cloud account added successfully.");
    }

    // Assign Cloud Account to User
    @GetMapping("/assigned/{userId}")
    public List<CloudAccountsDto> getAssignedAccounts(@PathVariable Long userId) {
        return cloudAccountService.getAssignedAccountsByUserId(userId);
    }

    @PostMapping("/add-user")
    public ResponseEntity<String> addUserWithRoleAndAccounts(@RequestBody CreateUserRequest request, Authentication authentication) {
        try {
            cloudAccountService.addUserWithRoleAndAccounts(request);
            return ResponseEntity.ok("User created successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(" " + e.getMessage());
        }
    }

    @PutMapping("/update-user/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        try {
            cloudAccountService.updateUser(userId, request);
            return ResponseEntity.ok("User updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("" + e.getMessage());
        }
    }
}
