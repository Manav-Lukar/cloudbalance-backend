package com.cloudbalance.service;

import com.cloudbalance.dto.*;
import com.cloudbalance.entity.User;
import com.cloudbalance.entity.Role;
import com.cloudbalance.entity.UserCloudAccountMap;
import com.cloudbalance.repository.RoleRepository;
import com.cloudbalance.repository.UserRepository;
import com.cloudbalance.repository.UserCloudAccountMapRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserCloudAccountMapRepository mappingRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    // User registration
    public boolean addUser(UserDTO userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return false; // email already exists
        }

        Optional<Role> optionalRole = roleRepository.findByName(userDTO.getRole().toUpperCase());
        if (optionalRole.isEmpty()) {
            return false; // role not found
        }

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(optionalRole.get());
        user.setLastLogin(null); // set to null initially

        userRepository.save(user);
        return true;
    }

    // User login (authentication + lastLoginTime update)
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                user.setLastLogin(LocalDateTime.now());

                // Generate refresh token and store it in the user record
                String refreshToken = generateRefreshToken(user.getEmail());
                user.setRefreshToken(refreshToken);
                userRepository.save(user); // Save the updated user with the refresh token

                return new LoginResponse(
                        user.getFirstName(),
                        user.getRole().getName(),
                        refreshToken,
                        user.getEmail(),
                        "✅ Login successful"
                );
            }
        }
        return null;
    }

    // Method to generate refresh token
    private String generateRefreshToken(String email) {
        long now = System.currentTimeMillis();
        long expirationMillis = 1000 * 60 * 15; // 15 minutes expiration

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, "secure-key") // Use a secure key
                .compact();
    }

    // Fetch all users with cloud accounts
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getLastLogin(),
                        user.getRole().getName(),
                        getCloudAccountsForUser(user)
                )).collect(Collectors.toList());
    }

    // Fetch user by ID with cloud accounts
    public UserResponseDTO getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return new UserResponseDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getLastLogin(),
                    user.getRole().getName(),
                    getCloudAccountsForUser(user)
            );
        }
        return null;
    }

    // Helper: get cloud accounts mapped to user
    private List<CloudAccountsDto> getCloudAccountsForUser(User user) {
        List<UserCloudAccountMap> mappings = mappingRepository.findByUser(user);
        return mappings.stream()
                .map(mapping -> {
                    var acc = mapping.getCloudAccount();
                    var dto = new CloudAccountsDto();
                    dto.setId(acc.getId());
                    dto.setAccountName(acc.getAccountName());
                    dto.setProvider(acc.getProvider());
                    dto.setAccountId(acc.getAccountId());
                    dto.setIsOrphaned(acc.getIsOrphaned());
                    return dto;
                }).collect(Collectors.toList());
    }

    public void blacklistRefreshToken(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRefreshToken(null); // Remove the refresh token
            user.setBlacklisted(true); // Mark user as blacklisted
            userRepository.save(user);
        }
    }

    public JwtResponse refreshAccessToken(String refreshToken) {
        String email = validateRefreshToken(refreshToken);
        if (email == null) {
            return null; // Invalid refresh token
        }

        String newAccessToken = generateAccessToken(email);
        return new JwtResponse(newAccessToken);
    }

    // Method to validate the refresh token
    private String validateRefreshToken(String refreshToken) {
        try {
            String email = Jwts.parser()
                    .setSigningKey("secure-key")
                    .parseClaimsJws(refreshToken)
                    .getBody()
                    .getSubject();
            return email;
        } catch (Exception e) {
            return null; // Invalid token
        }
    }

    // Generate new access token
    private String generateAccessToken(String email) {
        long now = System.currentTimeMillis();
        long expirationMillis = 1000 * 60 * 15; // 15 minutes expiration

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, "secure-key") // Use a secure key
                .compact();
    }


}
