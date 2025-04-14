package com.cloudbalance.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime lastLogin;
    private String role;

}
