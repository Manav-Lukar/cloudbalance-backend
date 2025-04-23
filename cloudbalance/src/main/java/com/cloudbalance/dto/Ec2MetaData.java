package com.cloudbalance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ec2MetaData {
    private String instanceId;
    private String name;
    private String region;
    private String state;

}
