package com.ghulam.backend.helper;

import com.ghulam.backend.dtos.ChatRequest;
import com.ghulam.backend.dtos.OllamaResponse;
import com.ghulam.backend.service.ChatService;
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
    private final ChatService chatService;

    public RunnerApplication(RedisTemplate<String, Object> redisTemplate, ChatClient chatClient, ChatService chatService) {
        this.redisTemplate = redisTemplate;
        this.chatClient = chatClient;
        this.chatService = chatService;
    }

    @PostConstruct
    public void init() {
        checkRedis();
        checkApiKey();
        // checkOllamaChat();
        // checkOllamaChatV2();
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

    public void checkOllamaChat() {
        String conversationId = UUID.randomUUID().toString();
        try {
            String response = chatClient
                    .prompt()
                    .user("What can you do?")
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

            log.info("{} Ollama Chat is UP", AppSetting.LOG_SEPARATOR);
            log.info("{} Ollama Chat Response: {}", AppSetting.LOG_SEPARATOR, response);
        } catch (Exception e) {
            log.error("{} Ollama Chat check FAILED", AppSetting.LOG_SEPARATOR, e);
        }
    }

    public void checkOllamaChatV2() {
        OllamaResponse out = chatService.chat(new ChatRequest("workspace", "userId", "conversationId", ""));
        log.info("{} Ollama Chat V2 Response: {}", AppSetting.LOG_SEPARATOR, out);
    }
}