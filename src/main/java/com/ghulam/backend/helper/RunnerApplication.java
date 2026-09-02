package com.ghulam.backend.helper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class RunnerApplication {


    private final RedisTemplate<String, Object> redisTemplate;

    public RunnerApplication(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        checkRedis();
    }

    public void checkRedis() {
        try {
            String key = "check:redis";
            String value = "ok";
            redisTemplate.opsForValue().set(key, value);
            String out = (String) redisTemplate.opsForValue().get(key);
            log.info("<<======================================================== Redis is up: key={}, value={}", key, out);
        } catch (Exception e) {
            log.error("<<======================================================== Redis check failed: {}", e.getMessage());
        }
    }
}