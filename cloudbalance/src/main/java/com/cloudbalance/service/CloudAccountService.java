package com.cloudbalance.service;
import org.springframework.security.core.Authentication;
import com.cloudbalance.dto.AssignRequest;
import com.cloudbalance.dto.CloudAccountsDto;
import com.cloudbalance.dto.CreateUserRequest;
import com.cloudbalance.dto.UpdateUserRequest;
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
import java.util.List;
import java.util.stream.Collectors;

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

    // Fetch all cloud accounts
    public List<CloudAccountsDto> getAllCloudAccountsDto() {
        List<CloudAccount> accounts = cloudAccountRepository.findAll();
        return accounts.stream().map(account -> {
            CloudAccountsDto dto = new CloudAccountsDto();
            dto.setId(account.getId());
            dto.setArnNumber(account.getArnNumber());
            dto.setAccountName(account.getAccountName());
            dto.setProvider(account.getProvider());
            dto.setAccountId(account.getAccountId());
            dto.setIsOrphaned(account.getIsOrphaned());
            return dto;
        }).collect(Collectors.toList());
    }

    // Fetch orphan cloud accounts
    public List<CloudAccount> getOrphanAccounts() {
        return cloudAccountRepository.findByIsOrphanedTrue();
    }

    // Add a new cloud account
    public void addCloudAccount(CloudAccountsDto dto) {
        CloudAccount account = CloudAccount.builder()
                .accountId(dto.getAccountId())
                .arnNumber(dto.getArnNumber())
                .accountName(dto.getAccountName())
                .provider(dto.getProvider())
                .isOrphaned(true) // Default to orphaned
                .build();

        cloudAccountRepository.save(account);
    }

    // Assign accounts to a user
    public void assignAccountsToUser(AssignRequest request, Authentication authentication) {
        String adminEmail = authentication.getName();
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().getName().equalsIgnoreCase("CUSTOMER")) {
            throw new RuntimeException("Only CUSTOMER users can be assigned accounts.");
        }

        List<CloudAccount> accounts = cloudAccountRepository.findAllById(request.getCloudAccountIds());
        for (CloudAccount account : accounts) {
            account.setIsOrphaned(false);
            UserCloudAccountMap mapping = UserCloudAccountMap.builder()
                    .user(user)
                    .cloudAccount(account)
                    .assignedBy(adminUser)
                    .build();
            mappingRepository.save(mapping);
        }
        cloudAccountRepository.saveAll(accounts);
    }

    public List<CloudAccountsDto> getAssignedAccountsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserCloudAccountMap> mappings = mappingRepository.findByUser(user);
        return mappings.stream()
                .map(mapping -> {
                    CloudAccount account = mapping.getCloudAccount();
                    CloudAccountsDto dto = new CloudAccountsDto();
                    dto.setId(account.getId());
                    dto.setArnNumber(account.getArnNumber());
                    dto.setAccountName(account.getAccountName());
                    dto.setProvider(account.getProvider());
                    dto.setAccountId(account.getAccountId());
                    dto.setIsOrphaned(account.getIsOrphaned());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Helper method to remove all cloud account associations for a user
    private void removeUserCloudAccountAssociations(User user) {
        // Get all mappings for this user
        List<UserCloudAccountMap> userMappings = mappingRepository.findByUser(user);

        // Get all cloud accounts associated with this user
        List<CloudAccount> associatedAccounts = userMappings.stream()
                .map(UserCloudAccountMap::getCloudAccount)
                .collect(Collectors.toList());

        // Delete all mappings for this user
        mappingRepository.deleteAll(userMappings);

        // Update the orphaned status of each account
        for (CloudAccount account : associatedAccounts) {
            // If there are no other mappings for this account, mark it as orphaned
            if (mappingRepository.findByCloudAccount(account).isEmpty()) {
                account.setIsOrphaned(true);
            }
        }

        // Save the updated cloud accounts
        if (!associatedAccounts.isEmpty()) {
            cloudAccountRepository.saveAll(associatedAccounts);
        }
    }

    // Create user with role and cloud accounts
    public void addUserWithRoleAndAccounts(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password must not be empty.");
        }

        Role role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found."));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // user-defined password
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
                        .assignedBy(user)
                        .build();
                mappingRepository.save(mapping);
            }
            cloudAccountRepository.saveAll(accounts);
        }
    }

    // Update user details
    public void updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean wasCustomer = user.getRole().getName().equalsIgnoreCase("CUSTOMER");
        boolean willBeCustomer = wasCustomer;

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        if (request.getEmail() != null && !request.getEmail().isEmpty() && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists.");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            Role role = roleRepository.findByName(request.getRole().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Role not found."));
            user.setRole(role);
            willBeCustomer = role.getName().equalsIgnoreCase("CUSTOMER");
        }

        if (wasCustomer && !willBeCustomer) {
            removeUserCloudAccountAssociations(user);
        }

        if (willBeCustomer && request.getCloudAccountIds() != null) {
            removeUserCloudAccountAssociations(user);

            if (!request.getCloudAccountIds().isEmpty()) {
                List<CloudAccount> accounts = cloudAccountRepository.findAllById(request.getCloudAccountIds());
                for (CloudAccount account : accounts) {
                    account.setIsOrphaned(false);
                    UserCloudAccountMap mapping = UserCloudAccountMap.builder()
                            .user(user)
                            .cloudAccount(account)
                            .assignedBy(user)
                            .build();
                    mappingRepository.save(mapping);
                }
                cloudAccountRepository.saveAll(accounts);
            }
        }

        userRepository.save(user);
    }
}
