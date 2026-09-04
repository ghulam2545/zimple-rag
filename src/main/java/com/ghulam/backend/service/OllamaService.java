package com.ghulam.backend.service;

import com.ghulam.backend.config.ChatConfiguration;
import com.ghulam.backend.dtos.DocumentReference;
import com.ghulam.backend.dtos.OllamaResponse;
import com.ghulam.backend.helper.AppSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OllamaService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RedisService redisService;

    public OllamaService(ChatClient chatClient, VectorStore vectorStore, RedisService redisService) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.redisService = redisService;
    }

    // chat with a given MD file
    public OllamaResponse chat(String conversationId, String filename, String query) {
        if (query.length() > AppSetting.MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException("Query is too long");
        }
        redisService.addMessage(conversationId, "user", query);

        List<Document> docs = similaritySearch(filename, query);

        if (docs.isEmpty()) {
            return new OllamaResponse(conversationId, "I don't have that in the knowledge base. Try uploading relevant MD files or rephrase.", List.of());
        }

        List<DocumentReference> sources = docs.stream().map(d -> new DocumentReference(
                d.getMetadata().getOrDefault("filename", "").toString(),
                d.getMetadata().getOrDefault("heading", "").toString(),
                Double.valueOf(d.getMetadata().getOrDefault("score", 0.0).toString())
        )).toList();

        String contexts = contextBlock(docs);
        String answer = getAnswer(conversationId, query, contexts);
        redisService.addMessage(conversationId, "assistant", answer);

        return new OllamaResponse(conversationId, answer, sources);
    }

    // simple chat via a string query
    public String simpleChat(String query) {
        String conversationId = UUID.randomUUID().toString();
        return chatClient
                .prompt()
                .user(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    // ────────────────────────────────────────────────────── private helpers

    private List<Document> similaritySearch(String filename, String query) {
        var builder = new FilterExpressionBuilder();
        Filter.Expression filter = builder.eq("filename", filename).build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(AppSetting.TOP_K)
                // .similarityThreshold(AppSetting.SIMILARITY_THRESHOLD)
                // .filterExpression(filter)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        log.info("{} Similarity search completed. Query='{}', results={}", AppSetting.LOG_SEPARATOR, query, documents.size());

        return documents;
    }

    private String contextBlock(List<Document> documents) {
        return documents.stream()
                .map(document -> {
                    String source = document.getMetadata().getOrDefault("filename", "").toString();
                    String heading = document.getMetadata().getOrDefault("heading", "").toString();

                    return """
                            ---
                            SOURCE: %s
                            HEADING: %s
                            CONTENT:
                            %s
                            """.formatted(
                            source,
                            heading,
                            document.getText()
                    );
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String getAnswer(String conversationId, String query, String context) {
        String prompt = """
                %s
                
                User Query:
                %s
                
                Retrieved Context:
                %s
                """.formatted(
                ChatConfiguration.DEFAULT_SYSTEM_MESSAGE,
                query,
                context
        );

        return chatClient
                .prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

}
