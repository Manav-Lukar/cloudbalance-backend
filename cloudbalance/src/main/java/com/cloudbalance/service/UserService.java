package com.cloudbalance.service;

import com.cloudbalance.dto.*;
import com.cloudbalance.entity.User;
import com.cloudbalance.entity.Role;
import com.cloudbalance.entity.UserCloudAccountMap;
import com.cloudbalance.repository.RoleRepository;
import com.cloudbalance.repository.UserRepository;
import com.cloudbalance.repository.UserCloudAccountMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private JwtService jwtService;

    // ✅ User registration
    public boolean addUser(UserDTO userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return false;
        }

        Optional<Role> optionalRole = roleRepository.findByName(userDTO.getRole().toUpperCase());
        if (optionalRole.isEmpty()) {
            return false;
        }

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(optionalRole.get());
        user.setLastLogin(null);

        userRepository.save(user);
        return true;
    }

    // ✅ Updated login returning ResponseEntity
    public ResponseEntity<?> login(LoginRequest loginRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                user.setLastLogin(LocalDateTime.now());

                String refreshToken = jwtService.generateRefreshToken(user.getEmail());
                user.setRefreshToken(refreshToken);

                userRepository.save(user);

                LoginResponse response = new LoginResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getRole().getName(),
                        refreshToken,
                        user.getEmail(),
                        "Login successful"
                );

                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    // ✅ Updated refresh token returning ResponseEntity
    public ResponseEntity<?> refreshToken(String refreshToken) {
        String email = jwtService.validateRefreshToken(refreshToken);
        if (email == null) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(email);
        return ResponseEntity.ok(new JwtResponse(newAccessToken));
    }

    //  Updated logout returning ResponseEntity
    public ResponseEntity<?> logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtService.validateRefreshToken(token);
            if (email != null) {
                Optional<User> optionalUser = userRepository.findByEmail(email);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    blacklistRefreshToken(user.getId());
                    return ResponseEntity.ok("Logged out successfully.");
                }
            }
            return ResponseEntity.status(401).body("Invalid token.");
        } else {
            return ResponseEntity.badRequest().body("No token provided.");
        }
    }

    // ✅ Existing methods
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getLastLogin() != null ? user.getLastLogin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null,
                        user.getRole().getName(),
                        getCloudAccountsForUser(user)
                )).collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return new UserResponseDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getLastLogin() != null ? user.getLastLogin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null,
                    user.getRole().getName(),
                    getCloudAccountsForUser(user)
            );
        }
        return null;
    }

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
            user.setRefreshToken(null);
            user.setBlacklisted(true);
            userRepository.save(user);
        }
    }
}
