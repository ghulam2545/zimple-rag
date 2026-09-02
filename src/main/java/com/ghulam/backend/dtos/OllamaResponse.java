package com.ghulam.backend.dtos;

import java.util.List;

public record OllamaResponse(
        String conversationId,
        String answer,
        List<DocumentReference> references
) {
}
