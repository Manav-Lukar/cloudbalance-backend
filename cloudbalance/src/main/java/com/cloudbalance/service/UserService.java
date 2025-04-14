package com.cloudbalance.service;


import com.cloudbalance.dto.UserDTO;
import com.cloudbalance.dto.UserResponseDTO;
import com.cloudbalance.entity.Role;
import com.cloudbalance.entity.User;
import com.cloudbalance.repository.RoleRepository;
import com.cloudbalance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public boolean authenticateUser(String email, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    // Fetch all users (admin only maybe)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getLastLogin(),
                        user.getRole().getName()
                )).collect(Collectors.toList());
    }
}
