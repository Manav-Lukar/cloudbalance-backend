package com.cloudbalance.controller;

import com.cloudbalance.dto.AssignRequest;
import com.cloudbalance.dto.CreateUserRequest;
import com.cloudbalance.entity.CloudAccount;
import com.cloudbalance.service.CloudAccountService;
import com.cloudbalance.repository.UserRepository;
import com.cloudbalance.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class CloudAccountAdminController {

    @Autowired
    private CloudAccountService cloudAccountService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/cloud-accounts")
    public ResponseEntity<List<CloudAccount>> getAllAccounts() {
        return ResponseEntity.ok(cloudAccountService.getAllCloudAccounts());
    }

    @GetMapping("/cloud-accounts/orphan")
    public ResponseEntity<List<CloudAccount>> getOrphanAccounts() {
        return ResponseEntity.ok(cloudAccountService.getOrphanAccounts());
    }

    @PostMapping("/assign-accounts")
    public ResponseEntity<String> assignAccountsToUser(@RequestBody AssignRequest request, Authentication authentication) {
        String adminEmail = authentication.getName();
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        cloudAccountService.assignAccounts(request.getUserId(), request.getCloudAccountIds(), adminUser);
        return ResponseEntity.ok("✅ Accounts assigned successfully.");
    }

    @PostMapping("/add-user")
    public ResponseEntity<String> addUserWithRoleAndAccounts(@RequestBody CreateUserRequest request, Authentication authentication) {
        try {
            String adminEmail = authentication.getName();
            User adminUser = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            cloudAccountService.createUserWithAccounts(request, adminUser);
            return ResponseEntity.ok("✅ User created successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    }