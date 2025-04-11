package com.cloudbalance.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "User Management", "AWS Services"

    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    private List<RoleDashboardAccessMap> accessMappings;

    // Constructors, getters, setters
}
