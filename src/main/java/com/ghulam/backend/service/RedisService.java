package com.ghulam.backend.service;

import com.ghulam.backend.helper.AppSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private String key(String conversationId) {
        return "chat:memory:" + conversationId;
    }

    public void addMessage(String conversationId, String role, String content) {
        String k = key(conversationId);
        ChatMessage msg = new ChatMessage(role, content, System.currentTimeMillis());
        redisTemplate.opsForList().rightPush(k, msg);
        redisTemplate.expire(k, Duration.ofHours(AppSetting.CHAT_MEMORY_TTL_HOURS));
        redisTemplate.opsForList().trim(k, -40, -1);
    }

    public List<ChatMessage> getHistory(String conversationId) {
        String k = key(conversationId);
        List<Object> raw = redisTemplate.opsForList().range(k, 0, -1);
        if (raw == null) return Collections.emptyList();
        return raw.stream().map(o -> (ChatMessage) o).toList();
    }

    public void clear(String conversationId) {
        redisTemplate.delete(key(conversationId));
    }

    public record ChatMessage(String role, String content, long timestamp) {
    }
}
