package com.cloudbalance.controller;

import com.cloudbalance.dto.RDSMetaData;
import com.cloudbalance.service.RdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rds")
public class RdsController {
    @Autowired
   private RdsService rdsService;

    @GetMapping("/metadata")
    public List<RDSMetaData> getMetadata(@RequestParam String roleArn,@RequestParam String region) {
        return rdsService.getRdsInstancesViaAssumedRole(roleArn, region);
    }
}
;