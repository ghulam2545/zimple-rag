package com.ghulam.backend.dtos;

public record IngestResponse(
        String fileName,
        String status,
        int chunks,
        String fileHash) {
}