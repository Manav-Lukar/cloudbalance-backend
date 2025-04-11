package com.cloudbalance.service;

import com.cloudbalance.dto.CreateUserRequest;
import com.cloudbalance.entity.CloudAccount;
import com.cloudbalance.entity.Role;
import com.cloudbalance.entity.User;
import com.cloudbalance.entity.UserCloudAccountMap;
import com.cloudbalance.repository.CloudAccountRepository;
import com.cloudbalance.repository.RoleRepository;
import com.cloudbalance.repository.UserCloudAccountMapRepository;
import com.cloudbalance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CloudAccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudAccountRepository cloudAccountRepository;

    @Autowired
    private UserCloudAccountMapRepository mappingRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<CloudAccount> getAllCloudAccounts() {
        return cloudAccountRepository.findAll();
    }

    public List<CloudAccount> getOrphanAccounts() {
        return cloudAccountRepository.findByIsOrphanedTrue();
    }

    public void assignAccounts(Long userId, List<Long> accountIds, User adminUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().getName().equalsIgnoreCase("CUSTOMER")) {
            throw new RuntimeException("Only CUSTOMER users can be assigned accounts.");
        }

        List<CloudAccount> accounts = cloudAccountRepository.findAllById(accountIds);
        for (CloudAccount account : accounts) {
            account.setIsOrphaned(false);

            UserCloudAccountMap mapping = UserCloudAccountMap.builder()
                    .user(user)
                    .cloudAccount(account)
                    .assignedBy(adminUser)
                    .assignedAt(LocalDateTime.now())
                    .build();

            mappingRepository.save(mapping);
        }

        cloudAccountRepository.saveAll(accounts);
    }

    public void createUserWithAccounts(CreateUserRequest request, User adminUser) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists.");
        }

        Role role = roleRepository.findByName(request.getRoleName().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found."));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode("Default@123")) // or use custom value
                .isActive(true)
                .role(role)
                .build();

        userRepository.save(user);

        if (role.getName().equalsIgnoreCase("CUSTOMER") && request.getCloudAccountIds() != null) {
            List<CloudAccount> accounts = cloudAccountRepository.findAllById(request.getCloudAccountIds());
            for (CloudAccount account : accounts) {
                account.setIsOrphaned(false);

                UserCloudAccountMap mapping = UserCloudAccountMap.builder()
                        .user(user)
                        .cloudAccount(account)
                        .assignedBy(adminUser)
                        .assignedAt(LocalDateTime.now())
                        .build();

                mappingRepository.save(mapping);
            }
            cloudAccountRepository.saveAll(accounts);
        }
    }
}
