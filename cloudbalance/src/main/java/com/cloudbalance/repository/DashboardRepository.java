package com.cloudbalance.repository;
import com.cloudbalance.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    Optional<Dashboard> findByName(String name);
}
