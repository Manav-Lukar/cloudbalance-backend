package com.cloudbalance.service;

import com.cloudbalance.dto.UserDTO;
import com.cloudbalance.dto.UserResponseDTO;
import com.cloudbalance.entity.User;
import com.cloudbalance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // ✅ Add this
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // ✅ Inject PasswordEncoder

    public boolean addUser(UserDTO userDTO) {
        Optional<User> existingUser = Optional.ofNullable(userRepository.findByEmail(userDTO.getEmail()));
        if (existingUser.isPresent()) return false;

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        // ✅ Encode the password
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // ✅ Prefix role with "ROLE_"
        String role = userDTO.getRole().toUpperCase();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        user.setRole(role);

        userRepository.save(user);
        return true;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole()
                )).collect(Collectors.toList());
    }
}
