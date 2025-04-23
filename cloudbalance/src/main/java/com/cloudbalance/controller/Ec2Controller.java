package com.cloudbalance.controller;



import com.cloudbalance.dto.Ec2MetaData;
import com.cloudbalance.service.Ec2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ec2")
public class Ec2Controller {

    @Autowired
    private Ec2Service ec2Service;

    @GetMapping("/metadata")
    public List<Ec2MetaData> getMetadata(@RequestParam String roleArn, @RequestParam String region) {
        return ec2Service.getEc2InstancesViaAssumedRole(roleArn, region);
    }
}