package com.cloudbalance.dto;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String lastLogin; // Changed from LocalDateTime to String
    private String role;
    private List<CloudAccountsDto> cloudAccounts;
}
