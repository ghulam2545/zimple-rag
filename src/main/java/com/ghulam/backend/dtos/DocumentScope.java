package com.ghulam.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record DocumentScope(
        @NotBlank String workspace,
        @NotBlank String userId,
        @NotBlank String filename
) {
}
