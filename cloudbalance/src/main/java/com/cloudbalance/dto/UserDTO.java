// File: com.cloudbalance.dto.UserDTO.java
package com.cloudbalance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String email;
    private String password;
    private String role;
}
