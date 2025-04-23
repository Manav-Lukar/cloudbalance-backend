package com.cloudbalance.controller;


import com.cloudbalance.dto.AsgMetaData;
import com.cloudbalance.service.ASGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/asg")
public class AsgController {

    @Autowired
    private ASGService asgService;

    @GetMapping("/metadata")
    public List<AsgMetaData> getMetadata(String roleArn, String region) {
        return asgService.getASGInstancesViaAssumeRole(roleArn, region);
    }
}
