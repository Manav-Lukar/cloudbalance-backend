package com.cloudbalance.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DynamicCostRequest {
    private String startDate;
    private String endDate;
    private String accountId;
    private String groupBy;  // Add this field
    private Map<String, Object> filters;
}
