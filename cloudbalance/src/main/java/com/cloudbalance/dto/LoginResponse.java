package com.cloudbalance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String firstName;
    private String role;
    private String token;
    private String email;
    private String message;

}
