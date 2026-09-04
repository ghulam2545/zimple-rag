package com.ghulam.backend.dtos;

public record ChatRequest(
        String workspace,
        String userId,
        String conversationId,
        String filename,
        String query) {
}