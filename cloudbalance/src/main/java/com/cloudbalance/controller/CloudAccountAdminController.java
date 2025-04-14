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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class CloudAccountAdminController {

    private static final Logger logger = LoggerFactory.getLogger(CloudAccountAdminController.class);

    @Autowired
    private CloudAccountService cloudAccountService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/cloud-accounts")
    public ResponseEntity<List<CloudAccountsDto>> getAllAccounts() {
        logger.info("📥 Fetching all cloud accounts");
        return ResponseEntity.ok(cloudAccountService.getAllCloudAccountsDto());
    }

    @GetMapping("/cloud-accounts/orphan")
    public ResponseEntity<List<CloudAccount>> getOrphanAccounts() {
        logger.info("📤 Fetching orphan accounts");
        return ResponseEntity.ok(cloudAccountService.getOrphanAccounts());
    }

    @PostMapping("/assign-accounts")
    public ResponseEntity<String> assignAccountsToUser(@RequestBody AssignRequest request, Authentication authentication) {
        String adminEmail = authentication.getName();
        logger.info("🔧 Assigning accounts by admin: {}", adminEmail);

        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> {
                    logger.error("🚫 Admin not found: {}", adminEmail);
                    return new RuntimeException("Admin not found");
                });

        adminUser.setLastLogin(LocalDateTime.now());
        userRepository.save(adminUser);

        cloudAccountService.assignAccounts(request.getUserId(), request.getCloudAccountIds(), adminUser);
        logger.info("✅ Accounts assigned successfully by admin: {}", adminEmail);
        return ResponseEntity.ok("✅ Accounts assigned successfully.");
    }

    @PostMapping("/add-user")
    public ResponseEntity<String> addUserWithRoleAndAccounts(@RequestBody CreateUserRequest request, Authentication authentication) {
        String adminEmail = authentication.getName();
        logger.info("➕ Admin {} is creating a new user", adminEmail);

        try {
            User adminUser = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> {
                        logger.error("🚫 Admin not found: {}", adminEmail);
                        return new RuntimeException("Admin not found");
                    });

            adminUser.setLastLogin(LocalDateTime.now());
            userRepository.save(adminUser);

            cloudAccountService.createUserWithAccounts(request, adminUser);
            logger.info("✅ User created successfully by admin: {}", adminEmail);
            return ResponseEntity.ok("✅ User created successfully.");
        } catch (RuntimeException e) {
            logger.error("🔥 Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }
}
