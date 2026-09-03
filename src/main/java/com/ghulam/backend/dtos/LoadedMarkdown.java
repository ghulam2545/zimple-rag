package com.ghulam.backend.dtos;

import org.springframework.ai.document.Document;

import java.util.List;

public record LoadedMarkdown(
        MarkdownMetadata metadata,
        String cleanedContent,
        List<Document> chunks) {
}