package com.cloudbalance.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

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
    @JsonBackReference // ✅ Prevent infinite loop with User.cloudAccounts
    private User user;

    @ManyToOne
    @JoinColumn(name = "cloud_account_id")
    private CloudAccount cloudAccount;

    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    @JsonBackReference(value = "assignedByRef") // ✅ Prevent recursion through assignedBy
    private User assignedBy; // Admin who assigned

}
