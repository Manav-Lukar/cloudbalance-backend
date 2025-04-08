package com.cloudbalance.repository;

import com.cloudbalance.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Optionals;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByCustomerEmail(String email);
}
