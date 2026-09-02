package com.ghulam.backend.service;

import com.ghulam.backend.dtos.ChatRequest;
import com.ghulam.backend.dtos.OllamaResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final OllamaService ollamaService;

    public ChatService(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    public OllamaResponse chat(ChatRequest request) {
        return ollamaService.chat();
    }
}
