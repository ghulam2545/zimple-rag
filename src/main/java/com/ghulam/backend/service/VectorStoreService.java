package com.ghulam.backend.service;

import com.ghulam.backend.dtos.DocumentScope;
import com.ghulam.backend.helper.AppSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
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
        log.info("{} Added {} documents to PGVector", AppSetting.LOG_SEPARATOR, documents.size());
    }

    public void deleteByFilename(DocumentScope documentScope) {
        try {
            String workspace = documentScope.workspace();
            String userId = documentScope.userId();
            String filename = documentScope.filename();

            var b = new FilterExpressionBuilder();
            Filter.Expression filter = b
                    .and(
                            b.and(
                                    b.eq("workspace", workspace),
                                    b.eq("user_id", userId)
                            ),
                            b.eq("filename", filename)
                    )
                    .build();
            vectorStore.delete(filter);
            log.info("{} Deleted vectors for file_path={}", AppSetting.LOG_SEPARATOR, filename);
        } catch (Exception e) {
            log.warn("{} Delete failed, might be first ingestion: {}", AppSetting.LOG_SEPARATOR, e.getMessage());
        }
    }
}