package com.ghulam.backend.helper;

import com.ghulam.backend.service.ChatService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class RunnerApplication {

    @Value("${app.ollama.cloud.api-key}")
    private String apiKey;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatService chatService;

    public RunnerApplication(RedisTemplate<String, Object> redisTemplate, ChatService chatService) {
        this.redisTemplate = redisTemplate;
        this.chatService = chatService;
    }

    @PostConstruct
    public void init() {
        checkRedis();
        checkApiKey();
        // checkSimpleChat();
        // checkSimpleEmbedding();
    }

    public void checkRedis() {
        try {
            String key = "check:redis";
            String value = "ok";
            redisTemplate.opsForValue().set(key, value);
            String out = (String) redisTemplate.opsForValue().get(key);
            log.info("{} Redis is up: key={}, value={}", AppSetting.LOG_SEPARATOR, key, out);
        } catch (Exception e) {
            log.error("{} Redis check failed: {}", AppSetting.LOG_SEPARATOR, e.getMessage());
        }
    }

    public void checkApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("{} Ollama API key is NOT configured", AppSetting.LOG_SEPARATOR);
        } else {
            log.info("{} Ollama API key is configured", AppSetting.LOG_SEPARATOR);
        }
    }

    public void checkSimpleChat() {
        try {
            String message = "hello";
            String out = chatService.simpleChat(message);
            log.info("{} Simple chat result: {}", AppSetting.LOG_SEPARATOR, out);
        } catch (Exception e) {
            log.error("{} Simple chat failed: {}", AppSetting.LOG_SEPARATOR, e.getMessage());
        }
    }

    public void checkSimpleEmbedding() {
        try {
            Document document = new Document("PostgreSQL is an open-source relational database system.");
            String query = "What is PostgreSQL?";

            String out = chatService.simpleEmbedding(document, query);
            log.info("{} Simple embedding result: {}", AppSetting.LOG_SEPARATOR, out);
        } catch (Exception e) {
            log.error("{} Simple embedding failed: {}", AppSetting.LOG_SEPARATOR, e.getMessage());
        }
    }
}
