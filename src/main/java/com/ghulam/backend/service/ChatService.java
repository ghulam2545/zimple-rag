package com.ghulam.backend.service;

import com.ghulam.backend.dtos.ChatRequest;
import com.ghulam.backend.dtos.OllamaResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final OllamaService ollamaService;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public ChatService(OllamaService ollamaService, VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.ollamaService = ollamaService;
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public OllamaResponse chat(ChatRequest request) {
        String conversationId = request.conversationId();
        String query = request.query();
        return ollamaService.chat(conversationId, request.documentScope(), query);
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

    public Map<String, Object> getIngestedFiles(int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        String dataSql = """
                SELECT workspace, user_id, filename, is_public, created_timestamp
                FROM document_data
                ORDER BY created_timestamp DESC
                LIMIT ? OFFSET ?
                """;

        String countSql = "SELECT COUNT(*) FROM document_data";

        var files = jdbcTemplate.queryForList(dataSql, pageSize, offset);
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        assert total != null;

        return Map.of(
                "files", files,
                "page", pageNumber,
                "size", pageSize,
                "total", total,
                "totalPages", (int) Math.ceil((double) total / pageSize)
        );
    }
}
