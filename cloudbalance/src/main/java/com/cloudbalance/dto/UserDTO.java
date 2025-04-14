package com.cloudbalance.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role; // e.g., ADMIN, READ_ONLY, CUSTOMER
    private LocalDateTime lastLogin;
}
