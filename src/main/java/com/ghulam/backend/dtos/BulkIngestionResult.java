package com.ghulam.backend.dtos;

public record BulkIngestionResult(
        int processed,
        int total,
        int totalChunks) {
}