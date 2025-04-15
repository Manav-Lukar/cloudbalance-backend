package com.cloudbalance.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users") // 'user' is a reserved keyword in many DBs
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName; // ✅ first name of the user

    @Column(nullable = false)
    private String lastName; // ✅ last name of the user

    @Column(unique = true, nullable = false)
    private String email; // ✅ email used for login

    @Column(nullable = false)
    private String password;

    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

//    @Column( length = 500)
//    private String refreshToken;
//
//    @Column(length = 500)
//    private String accessToken;
//
//    private boolean blacklisted = false;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference // ✅ Prevents recursive serialization
    private List<UserCloudAccountMap> cloudAccounts;


}
