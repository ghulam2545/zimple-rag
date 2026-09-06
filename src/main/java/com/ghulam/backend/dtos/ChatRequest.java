package com.ghulam.backend.dtos;

public record ChatRequest(
        DocumentScope documentScope,
        String conversationId,
        String query) {
}