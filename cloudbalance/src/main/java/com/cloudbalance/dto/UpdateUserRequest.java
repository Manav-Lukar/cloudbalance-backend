package com.cloudbalance.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private List<Long> cloudAccountIds;

}