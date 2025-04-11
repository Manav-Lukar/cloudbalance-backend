package com.cloudbalance.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CloudAccountsDto {
    private Long id;

    private String accountName;
    private String provider;
    private String accountId;

    private String accessKey;
    private String secretKey;

    private Boolean isOrphaned = true;

    private LocalDateTime createdAt = LocalDateTime.now();

}
