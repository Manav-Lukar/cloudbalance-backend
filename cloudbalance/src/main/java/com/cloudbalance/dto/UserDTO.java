package com.cloudbalance.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role; // e.g., ADMIN, READ_ONLY, CUSTOMER
}
