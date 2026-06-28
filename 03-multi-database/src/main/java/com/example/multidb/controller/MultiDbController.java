package com.example.multidb.controller;

import com.example.multidb.mysql.model.Order;
import com.example.multidb.mysql.repository.OrderRepository;
import com.example.multidb.pg.model.Customer;
import com.example.multidb.pg.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// One controller, two databases — both reached through the single OJP server.
@RestController
public class MultiDbController {

    private final CustomerRepository customers;   // PostgreSQL
    private final OrderRepository orders;          // MySQL

    public MultiDbController(CustomerRepository customers, OrderRepository orders) {
        this.customers = customers;
        this.orders = orders;
    }

    // ── PostgreSQL ──
    @GetMapping("/customers")
    public List<Customer> allCustomers() { return customers.findAll(); }

    @PostMapping("/customers")
    public Customer addCustomer(@RequestBody Customer c) { return customers.save(c); }

    // ── MySQL ──
    @GetMapping("/orders")
    public List<Order> allOrders() { return orders.findAll(); }

    @PostMapping("/orders")
    public Order addOrder(@RequestBody Order o) { return orders.save(o); }
}
