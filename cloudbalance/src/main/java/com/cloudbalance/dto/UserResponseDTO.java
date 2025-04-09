// src/main/java/com/cloudbalance/dto/UserResponseDTO.java
package com.cloudbalance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
@AllArgsConstructor
public class UserResponseDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
