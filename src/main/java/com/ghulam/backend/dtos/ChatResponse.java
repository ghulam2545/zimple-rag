package com.ghulam.backend.dtos;

import java.util.List;

public record ChatResponse(
        String workspace,
        String userId,
        String conversationId,
        String answer,
        List<DocumentReference> sources,
        long latencyMs) {
}