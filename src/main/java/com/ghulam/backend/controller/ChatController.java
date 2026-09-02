package com.ghulam.backend.controller;

import com.ghulam.backend.dtos.ChatRequest;
import com.ghulam.backend.dtos.ChatResponse;
import com.ghulam.backend.dtos.OllamaResponse;
import com.ghulam.backend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(path = "/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        long start = System.currentTimeMillis();
        OllamaResponse resp = chatService.chat(request);
        long latency = System.currentTimeMillis() - start;

        return new ChatResponse(
                request.workspace(),
                request.userId(),
                request.conversationId(),
                resp.answer(),
                resp.references(),
                latency
        );
    }
}
