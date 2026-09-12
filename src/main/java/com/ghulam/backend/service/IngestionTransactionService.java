package com.ghulam.backend.service;

import com.ghulam.backend.dtos.DocumentScope;
import com.ghulam.backend.dtos.IngestionResult;
import com.ghulam.backend.dtos.LoadedMarkdown;
import com.ghulam.backend.helper.AppSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionTransactionService {

    private final VectorStoreService vectorStoreService;
    private final JdbcTemplate jdbcTemplate;

    // Ingests a loaded Markdown document within a single transaction.
    @Transactional
    public IngestionResult ingestDocument(LoadedMarkdown document) {
        var metadata = document.metadata();
        var documentScope = metadata.getDocumentScope();

        String filename = documentScope.filename();
        String fileHash = metadata.getFileHash();

        if (isAlreadyIngested(documentScope, fileHash)) {
            log.info(
                    "{} Skipping unchanged file: {}",
                    AppSetting.LOG_SEPARATOR,
                    filename
            );

            return new IngestionResult(
                    filename,
                    fileHash,
                    0,
                    "SKIPPED_DUPLICATE"
            );
        }

        log.info(
                "{} Ingesting {} ({} chunks)",
                AppSetting.LOG_SEPARATOR,
                filename,
                document.chunks().size()
        );

        vectorStoreService.deleteByFilename(documentScope);
        vectorStoreService.addDocuments(document.chunks());

        saveIngestionStatus(document);

        return new IngestionResult(
                filename,
                fileHash,
                document.chunks().size(),
                "COMPLETED"
        );
    }

    private boolean isAlreadyIngested(DocumentScope documentScope, String fileHash) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM document_data
                            WHERE filename = ?
                              AND user_id = ?
                              AND workspace = ?
                              AND file_hash = ?
                              AND status = 'COMPLETED'
                        )
                        """,
                Boolean.class,
                documentScope.filename(),
                documentScope.userId(),
                documentScope.workspace(),
                fileHash
        );

        return Boolean.TRUE.equals(exists);
    }

    private void saveIngestionStatus(LoadedMarkdown document) {
        var metadata = document.metadata();
        var documentScope = metadata.getDocumentScope();

        jdbcTemplate.update(
                """
                        INSERT INTO document_data
                            (user_id, workspace, filename, file_hash, file_size, chunks_count, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (user_id, workspace, filename, file_hash)
                        DO UPDATE SET
                            status = 'COMPLETED',
                            chunks_count = EXCLUDED.chunks_count
                        """,
                documentScope.userId(),
                documentScope.workspace(),
                documentScope.filename(),
                metadata.getFileHash(),
                metadata.getFileSize(),
                document.chunks().size(),
                "COMPLETED"
        );
    }
}
