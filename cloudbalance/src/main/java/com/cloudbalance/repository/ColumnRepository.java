package com.cloudbalance.repository;

import com.cloudbalance.entity.Columns;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColumnRepository extends JpaRepository<Columns, Long> {

}
