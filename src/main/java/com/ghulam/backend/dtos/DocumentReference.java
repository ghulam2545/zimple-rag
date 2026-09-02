package com.ghulam.backend.dtos;

public record DocumentReference(
        String filename,
        String heading,
        Double score
) {
}
