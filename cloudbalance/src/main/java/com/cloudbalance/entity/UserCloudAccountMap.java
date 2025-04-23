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
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "cloud_account_id")
    private CloudAccount cloudAccount;

    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    @JsonBackReference(value = "assignedByRef")
    private User assignedBy;

}
