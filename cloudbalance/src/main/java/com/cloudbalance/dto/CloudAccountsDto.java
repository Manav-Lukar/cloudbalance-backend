package com.cloudbalance.dto;

import lombok.Data;

@Data
public class CloudAccountsDto {
    private Long id;
    private String accountName;
    private String arnNumber;
    private String provider;
    private String accountId;
    private Boolean isOrphaned;
}
