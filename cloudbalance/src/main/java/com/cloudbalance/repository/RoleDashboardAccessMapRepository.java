package com.cloudbalance.repository;

import com.cloudbalance.entity.AccessType;
import com.cloudbalance.entity.Dashboard;
import com.cloudbalance.entity.Role;
import com.cloudbalance.entity.RoleDashboardAccessMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleDashboardAccessMapRepository extends JpaRepository<RoleDashboardAccessMap, Long> {
    List<RoleDashboardAccessMap> findByRole(Role role);
    List<RoleDashboardAccessMap> findByDashboard(Dashboard dashboard);
    List<RoleDashboardAccessMap> findByRoleAndAccessType(Role role, AccessType accessType);
}
