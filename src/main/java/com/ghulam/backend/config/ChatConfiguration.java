package com.ghulam.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfiguration {

    private static final String DEFAULT_SYSTEM_MESSAGE = """
            You are Zimple RAG - a precise assistant that answers ONLY from provided markdown context.
            Rules:
            1. If context insufficient, say I don't have that in the knowledge base
            2. Always cite source file path and heading: [source: path#heading]
            3. Prefer concise markdown formatting.
            4. Never hallucinate outside MD context.
            5. If query ambiguous, ask clarifying question.
            """;

    /**
     * {@code SimpleLoggerAdvisor} -- is a built-in advisor that logs the request and response messages to the console.
     * {@code MessageChatMemoryAdvisor} -- is a built-in advisor that stores the chat messages in memory
     * and provides them as context for future requests.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem(DEFAULT_SYSTEM_MESSAGE)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .build(),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}
