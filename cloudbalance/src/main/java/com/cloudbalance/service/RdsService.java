package com.cloudbalance.service;

import com.cloudbalance.dto.RDSMetaData;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class RdsService {

    public List<RDSMetaData> getRdsInstancesViaAssumedRole(String roleArn, String region) {
        try {
            StsClient stsClient = StsClient.builder()
                    .region(Region.of(region))
                    .build();

            AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName("springbootRdsSession")
                    .build();

            AssumeRoleResponse assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest);

            AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(
                    assumeRoleResponse.credentials().accessKeyId(),
                    assumeRoleResponse.credentials().secretAccessKey(),
                    assumeRoleResponse.credentials().sessionToken()
            );

            RdsClient rdsClient = RdsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(sessionCredentials))
                    .build();

            DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
            List<RDSMetaData> rdsList = new ArrayList<>();

            for (DBInstance db : response.dbInstances()) {
                rdsList.add(new RDSMetaData(
                        db.dbInstanceArn(),
                        db.dbInstanceClass(),
                        db.engine(),
                        db.dbInstanceStatus(),
                        region
                ));
            }

            if (rdsList.isEmpty()) {
                throw new RuntimeException("No RDS instances found for role: " + roleArn);
            }

            return rdsList;

        } catch (StsException | RdsException e) {
            // Log the error
            System.err.println("Failed to fetch RDS instances: " + e.getMessage());
            // Return empty list or custom error message instead of 500 error
            return new ArrayList<>();
        }
    }
}
