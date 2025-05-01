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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@EnableMethodSecurity
@RequestMapping("/admin")
public class CloudAccountAdminController {

    @Autowired
    private CloudAccountService cloudAccountService;

    @GetMapping("/cloud-accounts")
    public ResponseEntity<List<CloudAccountsDto>> getAllAccounts() {
        return ResponseEntity.ok(cloudAccountService.getAllCloudAccountsDto());
    }

//    @GetMapping("/cloud-accounts/orphan")
//    public ResponseEntity<List<CloudAccount>> getOrphanAccounts() {
//        return ResponseEntity.ok(cloudAccountService.getOrphanAccounts());
//    }

    // Add Cloud Account
    @PostMapping("/add-cloud-accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addCloudAccount(@RequestBody CloudAccountsDto dto) {
        cloudAccountService.addCloudAccount(dto);
        return ResponseEntity.ok("Cloud account added successfully.");
    }

    // Assign Cloud Account to User
    @GetMapping("/assigned/{userId}")
    public List<CloudAccountsDto> getAssignedAccounts(@PathVariable Long userId) {
        return cloudAccountService.getAssignedAccountsByUserId(userId);
    }

}
