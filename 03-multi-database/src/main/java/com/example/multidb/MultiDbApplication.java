package com.example.multidb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// We define datasources manually, so disable Spring's single-datasource autoconfig.
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MultiDbApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultiDbApplication.class, args);
    }
}
