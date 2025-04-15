package com.cloudbalance.controller;

import com.cloudbalance.dto.AssignRequest;
import com.cloudbalance.dto.CloudAccountsDto;
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

import java.time.LocalDateTime;
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
    public ResponseEntity<List<CloudAccountsDto>> getAllAccounts() {
        return ResponseEntity.ok(cloudAccountService.getAllCloudAccountsDto());
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

        // ✅ Update last login time
        adminUser.setLastLogin(LocalDateTime.now());
        userRepository.save(adminUser);

        cloudAccountService.assignAccounts(request.getUserId(), request.getCloudAccountIds(), adminUser);
        return ResponseEntity.ok("✅ Accounts assigned successfully.");
    }

    @PostMapping("/add-user")
    public ResponseEntity<String> addUserWithRoleAndAccounts(@RequestBody CreateUserRequest request, Authentication authentication) {
        try {
            String adminEmail = authentication.getName();
            User adminUser = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            // ✅ Update last login time
            adminUser.setLastLogin(LocalDateTime.now());
            userRepository.save(adminUser);

            cloudAccountService.createUserWithAccounts(request, adminUser);
            return ResponseEntity.ok("✅ User created successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }


    }