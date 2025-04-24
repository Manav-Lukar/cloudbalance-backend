package com.cloudbalance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "snowflake_group_by")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service")
    private String service;

    @Column(name = "instance_type")
    private String instanceType;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "usage_type")
    private String usageType;

    @Column(name = "platform")
    private String platform;

    @Column(name = "region")
    private String region;

    @Column(name = "usage_type_group")
    private String usageTypeGroup;

    @Column(name = "tags", length = 1000)
    private String tags;
}
