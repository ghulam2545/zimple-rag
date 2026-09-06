package com.ghulam.backend.dtos;

public record DocumentScope(
        String workspace,
        String userId,
        String filename
) {
}
