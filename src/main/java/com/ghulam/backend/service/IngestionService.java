package com.ghulam.backend.service;

import com.ghulam.backend.dtos.DocumentScope;
import com.ghulam.backend.dtos.IngestionResult;
import com.ghulam.backend.helper.MarkdownDocumentLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
}