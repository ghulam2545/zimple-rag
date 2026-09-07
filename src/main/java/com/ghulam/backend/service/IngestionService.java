package com.ghulam.backend.service;

import com.ghulam.backend.dtos.BulkIngestionResult;
import com.ghulam.backend.dtos.DocumentScope;
import com.ghulam.backend.dtos.IngestionResult;
import com.ghulam.backend.helper.AppSetting;
import com.ghulam.backend.helper.MarkdownDocumentLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
    private final IngestionTransactionService transactionService;

    // Ingests a Markdown file uploaded by the user.
    public IngestionResult ingestUploadedFile(MultipartFile file, DocumentScope documentScope) throws IOException {
        var document = documentLoader.loadFromUpload(file, documentScope);
        return transactionService.ingestDocument(document);
    }

    // Ingests all Markdown files found recursively inside the given directory.
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
                log.warn(
                        "{} Failed to ingest {}: {}",
                        AppSetting.LOG_SEPARATOR,
                        file,
                        ex.getMessage()
                );
            }
        }

        return new BulkIngestionResult(
                processedFiles,
                markdownFiles.size(),
                totalChunks
        );
    }

    // Loads and ingests a Markdown file from the given path.
    public IngestionResult ingestFile(Path file) throws IOException {
        var document = documentLoader.loadFromPath(file);
        return transactionService.ingestDocument(document);
    }

    // Asynchronously loads and ingests a Markdown file.
    @Async
    public CompletableFuture<IngestionResult> ingestFileAsync(Path file) {
        try {
            return CompletableFuture.completedFuture(ingestFile(file));
        } catch (Exception ex) {
            log.error(
                    "{} Async ingestion failed for {}",
                    AppSetting.LOG_SEPARATOR,
                    file,
                    ex
            );

            return CompletableFuture.failedFuture(ex);
        }
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
}