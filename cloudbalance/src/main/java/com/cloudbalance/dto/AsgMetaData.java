package com.cloudbalance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsgMetaData {
    private String resourceId;
    private String resourceName;
    private String region;
    private int desiredCapacity;
    private int minSize;
    private int maxSize;
    private String status;
}
