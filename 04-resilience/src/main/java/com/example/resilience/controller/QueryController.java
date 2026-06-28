package com.example.resilience.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Endpoints that deliberately produce FAST, SLOW, and FAILING queries so you can
// watch OJP's circuit breaker and slow-query segregation react.
@RestController
public class QueryController {

    private final JdbcTemplate jdbc;

    public QueryController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // A fast OLTP-style query. Under slow-query segregation these keep flowing
    // even while slow queries are saturating their own lane.
    @GetMapping("/fast")
    public Map<String, Object> fast() {
        Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of("type", "fast", "result", one);
    }

    // A deliberately slow query using pg_sleep. Fire several of these concurrently
    // to occupy connections; without segregation they'd starve /fast.
    @GetMapping("/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "5") int seconds) {
        jdbc.execute("SELECT pg_sleep(" + seconds + ")");
        return Map.of("type", "slow", "sleptSeconds", seconds);
    }

    // A failing query. Repeated calls should trip OJP's circuit breaker so the
    // database stops being hammered by the same broken statement.
    @GetMapping("/broken")
    public Map<String, Object> broken() {
        jdbc.execute("SELECT * FROM table_that_does_not_exist");
        return Map.of("type", "broken");
    }
}
