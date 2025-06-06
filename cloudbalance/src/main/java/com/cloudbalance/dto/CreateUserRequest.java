package com.cloudbalance.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private List<Long> cloudAccountIds;
}
