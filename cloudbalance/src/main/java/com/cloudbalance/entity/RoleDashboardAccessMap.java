package com.cloudbalance.entity;

import jakarta.persistence.*;

@Entity
public class RoleDashboardAccessMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "dashboard_id")
    private Dashboard dashboard;

}
