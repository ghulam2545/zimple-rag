package com.ghulam.backend.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbc;
    private final RedisTemplate<String, Object> redis;

    public HealthController(JdbcTemplate jdbc, RedisTemplate<String, Object> redis) {
        this.jdbc = jdbc;
        this.redis = redis;
    }

    @GetMapping(path = "/health")
    public Map<String, Object> health() {
        Map<String, Object> out = new HashMap<>();

        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            out.put("postgres", "UP");
        } catch (Exception e) {
            out.put("postgres", "DOWN: " + e.getMessage());
        }

        try {
            assert redis.getConnectionFactory() != null;
            redis.getConnectionFactory().getConnection().ping();
            out.put("redis", "UP");
        } catch (Exception e) {
            out.put("redis", "DOWN: " + e.getMessage());
        }

        return out;
    }
}