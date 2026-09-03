package com.ghulam.backend.dtos;

public record IngestionResult(
        String fileName,
        String fileHash,
        int chunksCount,
        String status) {
}