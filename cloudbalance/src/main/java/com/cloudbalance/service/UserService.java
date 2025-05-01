package com.cloudbalance.service;

import com.cloudbalance.Exception.DuplicateResourceException;
import com.cloudbalance.Exception.ResourceNotFoundException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    // Improved user registration with exception handling
    public void addUser(UserDTO userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", userDTO.getEmail());
        }

        Role role = roleRepository.findByName(userDTO.getRole().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", userDTO.getRole().toUpperCase()));

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(role);
        user.setLastLogin(null);

        userRepository.save(user);
    }

    // Improved login with exception handling
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setLastLogin(LocalDateTime.now());

        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        user.setRefreshToken(refreshToken);

        userRepository.save(user);

        return new LoginResponse(
                user.getId(),
                user.getFirstName(),
                user.getRole().getName(),
                refreshToken,
                user.getEmail(),
                "Login successful"
        );
    }

    // Improved refresh token with exception handling
    public JwtResponse refreshToken(String refreshToken) {
        String email = jwtService.validateRefreshToken(refreshToken);
        if (email == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(email);
        return new JwtResponse(newAccessToken);
    }

    // Improved logout with exception handling
    public ResponseEntity<?> logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("No token provided or invalid token format");
        }

        String token = authHeader.substring(7);
        String email = jwtService.validateRefreshToken(token);
        if (email == null) {
            throw new BadCredentialsException("Invalid token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        blacklistRefreshToken(user.getId());
        return null;
    }

    // Improved getAllUsers
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

    // Improved getUserById with exception handling
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setRefreshToken(null);
        user.setBlacklisted(true);
        userRepository.save(user);
    }
}