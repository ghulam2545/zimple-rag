package com.ghulam.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentScope(
        @NotNull String workspace,
        @NotBlank String userId,
        @NotBlank String filename
) {
}
