package com.ghulam.backend.helper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public final class RunnerApplication {

    @Value("${app.ollama.cloud.api-key}")
    private String apiKey;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatClient chatClient;

    public RunnerApplication(RedisTemplate<String, Object> redisTemplate, ChatClient chatClient) {
        this.redisTemplate = redisTemplate;
        this.chatClient = chatClient;
    }

    @PostConstruct
    public void init() {
        checkRedis();
        checkApiKey();
        checkOllamaChat();
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

    public void checkApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("<<======================================================== Ollama API key is NOT configured");
        } else {
            log.info("<<======================================================== Ollama API key is configured");
        }
    }

    public void checkOllamaChat() {
        String conversationId = UUID.randomUUID().toString();
        try {
            String response = chatClient
                    .prompt()
                    .user("What can you do?")
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

            log.info("<<======================================================== Ollama Chat is UP");
            log.info("<<======================================================== Ollama Chat Response: {}", response);
        } catch (Exception e) {
            log.error("<<======================================================== Ollama Chat check FAILED", e);
        }
    }
}