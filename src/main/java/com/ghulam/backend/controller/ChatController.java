package com.ghulam.backend.controller;

import com.ghulam.backend.dtos.ChatRequest;
import com.ghulam.backend.dtos.ChatResponse;
import com.ghulam.backend.dtos.OllamaResponse;
import com.ghulam.backend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(path = "/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        long start = System.currentTimeMillis();
        OllamaResponse resp = chatService.chat(request);
        long latency = System.currentTimeMillis() - start;

        var out = new ChatResponse(
                request.documentScope().workspace(),
                request.documentScope().userId(),
                request.conversationId(),
                resp.answer(),
                resp.references(),
                latency
        );

        return ResponseEntity.ok(out);
    }

    @GetMapping(path = "/files")
    public ResponseEntity<?> getIngestedFiles() {
        var files = chatService.getIngestedFiles();
        return ResponseEntity.ok(files);
    }
}
