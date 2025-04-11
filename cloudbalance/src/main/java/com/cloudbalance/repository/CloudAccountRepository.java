package com.cloudbalance.repository;

import com.cloudbalance.entity.CloudAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CloudAccountRepository extends JpaRepository<CloudAccount, Long> {
    List<CloudAccount> findByIsOrphanedTrue();
}
