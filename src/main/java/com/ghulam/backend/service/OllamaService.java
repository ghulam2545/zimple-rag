package com.ghulam.backend.service;

import com.ghulam.backend.dtos.OllamaResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OllamaService {

    private final ChatClient chatClient;

    public OllamaService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public OllamaResponse chat() {
        // TODO
        String conversationId = UUID.randomUUID().toString();
        String response = "LOL"; //chatClient
                // .prompt()
                // .user("Who are you and what is your job?")
                // .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                // .call()
                // .content();

        return new OllamaResponse(conversationId, response, List.of());
    }

}
