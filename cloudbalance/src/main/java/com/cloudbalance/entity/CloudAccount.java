package com.cloudbalance.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String arnNumber;
    private String accountName;
    private String provider;
    private String accountId;

    @Builder.Default
    private Boolean isOrphaned = true;

    @OneToMany(mappedBy = "cloudAccount", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<UserCloudAccountMap> userMappings;

    @OneToMany(mappedBy = "cloudAccount", cascade = CascadeType.ALL)
    private List<SessionLog> onboardingLogs;

}
