package com.cloudbalance.repository;

import com.cloudbalance.entity.User;
import com.cloudbalance.entity.UserCloudAccountMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCloudAccountMapRepository extends JpaRepository<UserCloudAccountMap, Long> {
    List<UserCloudAccountMap> findByUser(User user);

}
