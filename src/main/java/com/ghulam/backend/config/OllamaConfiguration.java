package com.ghulam.backend.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class OllamaConfiguration {

    @Value("${app.ollama.cloud.base-url}")
    private String cloudBaseUrl;

    @Value("${app.ollama.cloud.api-key}")
    private String cloudApiKey;

    @Value("${app.ollama.cloud.chat-model}")
    private String cloudChatModel;

    @Value("${app.ollama.local.base-url}")
    private String localBaseUrl;

    @Value("${app.ollama.local.embedding-model}")
    private String localEmbeddingModel;


    // Connect to Ollama Cloud using the API key.
    @Bean("cloudOllamaApi")
    public OllamaApi cloudOllamaApi() {
        return OllamaApi.builder()
                .baseUrl(cloudBaseUrl)
                .restClientBuilder(
                        RestClient.builder()
                                .defaultHeader(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + cloudApiKey))
                .build();
    }


    // Connect to the local Ollama server.
    @Bean("localOllamaApi")
    public OllamaApi localOllamaApi() {
        return OllamaApi.builder()
                .baseUrl(localBaseUrl)
                .build();
    }


    // Use Ollama Cloud for chat requests.
    @Bean
    @Primary
    public ChatModel chatModel(@Qualifier("cloudOllamaApi") OllamaApi api) {

        return OllamaChatModel.builder()
                .ollamaApi(api)
                .options(
                        OllamaChatOptions.builder()
                                .model(cloudChatModel)
                                .build())
                .build();
    }


    // Use local Ollama for generating embeddings.
    @Bean
    @Primary
    public EmbeddingModel embeddingModel(@Qualifier("localOllamaApi") OllamaApi api) {

        return OllamaEmbeddingModel.builder()
                .ollamaApi(api)
                .options(
                        OllamaEmbeddingOptions.builder()
                                .model(localEmbeddingModel)
                                .build())
                .build();
    }

}
