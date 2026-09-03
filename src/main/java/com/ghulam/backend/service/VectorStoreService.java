package com.ghulam.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VectorStoreService {

    private static final int BATCH_SIZE = 100;
    private final VectorStore vectorStore;

    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) return;
        for (int i = 0; i < documents.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, documents.size());
            List<Document> batch = documents.subList(i, end);
            vectorStore.add(batch);
        }
        log.info("Added {} documents to PGVector", documents.size());
    }

    public void deleteByFilePath(String filePath) {
        try {
            vectorStore.delete("file_path == '" + filePath.replace("'", "''") + "'");
            log.info("Deleted vectors for file_path={}", filePath);
        } catch (Exception e) {
            log.warn("Delete failed, might be first ingestion: {}", e.getMessage());
        }
    }
}