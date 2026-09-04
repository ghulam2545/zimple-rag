package com.ghulam.backend.service;

import com.ghulam.backend.dtos.ChatRequest;
import com.ghulam.backend.dtos.OllamaResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final OllamaService ollamaService;
    private final VectorStore vectorStore;

    public ChatService(OllamaService ollamaService, VectorStore vectorStore) {
        this.ollamaService = ollamaService;
        this.vectorStore = vectorStore;
    }

    public OllamaResponse chat(ChatRequest request) {
        String conversationId = request.conversationId();
        String query = request.query();
        String filename = request.filename();
        return ollamaService.chat(conversationId, filename, query);
    }

    public String simpleChat(String query) {
        return ollamaService.simpleChat(query);
    }

    public String simpleEmbedding(Document document, String query) {
        vectorStore.add(List.of(document));

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(1)
                        .build()
        );

        return results.get(0).getText();
    }
}
