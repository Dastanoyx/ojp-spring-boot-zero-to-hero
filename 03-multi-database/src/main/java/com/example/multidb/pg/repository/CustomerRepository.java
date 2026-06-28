package com.example.multidb.pg.repository;

import com.example.multidb.pg.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
