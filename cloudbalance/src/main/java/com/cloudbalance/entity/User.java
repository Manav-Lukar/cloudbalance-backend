package com.cloudbalance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;  // Used for login

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;  // ROLE_ADMIN, ROLE_READ_ONLY, ROLE_CUSTOMER
}
