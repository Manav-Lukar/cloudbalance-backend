package com.cloudbalance.service;

import com.cloudbalance.dto.AsgMetaData;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class ASGService {
    public List<AsgMetaData> getASGInstancesViaAssumeRole(String roleArn, String region) {
        StsClient stsClient = StsClient.builder()
                .region(Region.of(region))
                .build();

        AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .roleSessionName("springbootAsgSession")
                .build();

        AssumeRoleResponse assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest);

        AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(
                assumeRoleResponse.credentials().accessKeyId(),
                assumeRoleResponse.credentials().secretAccessKey(),
                assumeRoleResponse.credentials().sessionToken()
        );

        AutoScalingClient autoScalingClient = AutoScalingClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(sessionCredentials))
                .build();

        DescribeAutoScalingGroupsResponse response = autoScalingClient.describeAutoScalingGroups(
                DescribeAutoScalingGroupsRequest.builder().build()
        );

        List<AsgMetaData> asgList = new ArrayList<>();

        for (AutoScalingGroup asg : response.autoScalingGroups()) {
            asgList.add(new AsgMetaData(
                    asg.serviceLinkedRoleARN(),
                    asg.autoScalingGroupName(),
                    region,
                    asg.desiredCapacity(),
                    asg.minSize(),
                    asg.maxSize(),
                    asg.status() != null ? asg.status() : "N/A"
            ));
        }
        return asgList;
    }
}
