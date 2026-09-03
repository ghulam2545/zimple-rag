package com.ghulam.backend.service;

import com.ghulam.backend.dtos.BulkIngestionResult;
import com.ghulam.backend.dtos.IngestionResult;
import com.ghulam.backend.dtos.LoadedMarkdown;
import com.ghulam.backend.helper.AppSetting;
import com.ghulam.backend.helper.MarkdownDocumentLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

    private final MarkdownDocumentLoader documentLoader;
    private final VectorStoreService vectorStoreService;
    private final JdbcTemplate jdbcTemplate;

    public IngestionResult ingestUpload(MultipartFile file) throws IOException {
        var document = documentLoader.loadFromUpload(file);
        return ingest(document);
    }

    public BulkIngestionResult ingestDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }

        List<Path> markdownFiles = findMarkdownFiles(directory);
        int processedFiles = 0;
        int totalChunks = 0;

        for (Path file : markdownFiles) {
            try {
                var result = ingestFile(file);
                if ("COMPLETED".equals(result.status())) {
                    processedFiles++;
                    totalChunks += result.chunksCount();
                }
            } catch (Exception ex) {
                log.warn("{} Failed to ingest {}: {}", AppSetting.LOG_SEPARATOR, file, ex.getMessage());
            }
        }

        return new BulkIngestionResult(processedFiles, markdownFiles.size(), totalChunks);
    }

    @Transactional
    public IngestionResult ingestFile(Path file) throws IOException {
        var document = documentLoader.loadFromPath(file);
        return ingest(document);
    }

    private IngestionResult ingest(LoadedMarkdown document) {
        var metadata = document.metadata();

        if (isAlreadyIngested(metadata.getFilePath(), metadata.getFileHash())) {
            log.info("{} Skipping unchanged file: {}", AppSetting.LOG_SEPARATOR, metadata.getFileName());
            return new IngestionResult(metadata.getFileName(), metadata.getFileHash(), 0, "SKIPPED_DUPLICATE");
        }
        log.info("{} Ingesting {} ({} chunks)", AppSetting.LOG_SEPARATOR, metadata.getFileName(), document.chunks().size());

        vectorStoreService.deleteByFilePath(metadata.getFilePath());
        vectorStoreService.addDocuments(document.chunks());

        saveIngestionStatus(document);
        return new IngestionResult(metadata.getFileName(), metadata.getFileHash(), document.chunks().size(), "COMPLETED");
    }

    private boolean isAlreadyIngested(String filePath, String fileHash) {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM document_data
                            WHERE file_path = ?
                              AND file_hash = ?
                              AND status = 'COMPLETED'
                        )
                        """,
                Boolean.class,
                filePath,
                fileHash
        );

        return Boolean.TRUE.equals(exists);
    }

    private void saveIngestionStatus(LoadedMarkdown document) {
        var metadata = document.metadata();
        jdbcTemplate.update(
                """
                        INSERT INTO document_data
                            (user_id, workspace, file_path, file_hash, file_size, chunks_count, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (user_id, workspace, file_path, file_hash)
                        DO UPDATE SET
                            status = 'COMPLETED',
                            chunks_count = EXCLUDED.chunks_count
                        """,
                metadata.getUserId(),
                metadata.getWorkspace(),
                metadata.getFilePath(),
                metadata.getFileHash(),
                metadata.getFileSize(),
                document.chunks().size(),
                "COMPLETED"
        );
    }

    private List<Path> findMarkdownFiles(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(this::isMarkdownFile)
                    .toList();
        }
    }

    private boolean isMarkdownFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".md") || fileName.endsWith(".markdown");
    }

    @Async
    public CompletableFuture<IngestionResult> ingestFileAsync(Path file) {
        try {
            return CompletableFuture.completedFuture(ingestFile(file));
        } catch (Exception ex) {
            log.error("{} Async ingest failed {}", AppSetting.LOG_SEPARATOR, file, ex);
            return CompletableFuture.failedFuture(ex);
        }
    }
}