package com.cloudbalance.dto;


import lombok.Data;
import java.util.List;

@Data
public class AssignRequest {
    private Long userId;
    private List<Long> cloudAccountIds;
}
