package com.cloudbalance.service;

import com.cloudbalance.dto.UserDTO;
import com.cloudbalance.dto.UserResponseDTO;
import com.cloudbalance.entity.User;
import com.cloudbalance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void createUser(UserDTO userDTO) {
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Ensure role starts with "ROLE_"
        String role = userDTO.getRole();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        user.setRole(role);

        userRepository.save(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserResponseDTO(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }

}
