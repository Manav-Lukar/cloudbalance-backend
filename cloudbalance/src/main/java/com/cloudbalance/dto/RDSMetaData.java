package com.cloudbalance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RDSMetaData {
    private String resourceId;
    private String resourceName;
    private String engine;
    private String status;
    private String region;
}
