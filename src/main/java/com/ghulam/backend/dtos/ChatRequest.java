package com.ghulam.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank DocumentScope documentScope,
        @NotBlank String conversationId,
        @NotBlank String query) {
}