package com.cloudbalance.entity;

import com.cloudbalance.entity.UserCloudAccountMap;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    private String accountName;
    private String provider;
    private String accountId;

    private String accessKey;
    private String secretKey;

    private Boolean isOrphaned = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "cloudAccount")
    private List<UserCloudAccountMap> userMappings;

    @OneToMany(mappedBy = "cloudAccount", cascade = CascadeType.ALL)
    private List<SessionLog> onboardingLogs;


}
