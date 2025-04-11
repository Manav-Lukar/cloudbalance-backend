package com.cloudbalance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCloudAccountMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "cloud_account_id")
    private CloudAccount cloudAccount;

    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    private User assignedBy; // Admin who assigned

    private LocalDateTime assignedAt = LocalDateTime.now();
}
