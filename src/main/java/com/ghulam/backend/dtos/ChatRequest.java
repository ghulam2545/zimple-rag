package com.ghulam.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
        @NotNull DocumentScope documentScope,
        @NotBlank String conversationId,
        @NotBlank String query) {
}