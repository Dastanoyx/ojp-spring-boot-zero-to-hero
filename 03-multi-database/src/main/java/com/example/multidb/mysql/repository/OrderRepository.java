package com.example.multidb.mysql.repository;

import com.example.multidb.mysql.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
