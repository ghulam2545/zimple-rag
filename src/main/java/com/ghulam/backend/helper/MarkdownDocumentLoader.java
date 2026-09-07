package com.ghulam.backend.helper;

import com.ghulam.backend.dtos.DocumentScope;
import com.ghulam.backend.dtos.LoadedMarkdown;
import com.ghulam.backend.dtos.MarkdownMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarkdownDocumentLoader {

    private final MarkdownValidator validator;
    private final MarkdownCleaner cleaner;
    private final MarkdownChunker chunker;

    public LoadedMarkdown loadFromUpload(MultipartFile file, DocumentScope documentScope) throws IOException {
        validator.validateUpload(file);
        String raw = new String(file.getBytes(), StandardCharsets.UTF_8);
        return process(raw, documentScope);
    }

    private LoadedMarkdown process(String raw, DocumentScope documentScope) {
        if (raw == null || raw.isBlank()) throw new RuntimeException("Empty markdown");

        Map<String, Object> fm = cleaner.extractFrontmatter(raw);
        String cleaned = cleaner.clean(raw);
        String hash = DigestUtils.md5DigestAsHex(cleaned.getBytes(StandardCharsets.UTF_8));

        MarkdownMetadata baseMeta = MarkdownMetadata.builder()
                .documentScope(documentScope).fileHash(hash)
                .fileSize(cleaned.length()).frontmatter(fm).build();

        List<Document> chunks = chunker.chunk(cleaned, baseMeta);

        log.info("{} Loaded MD {} -> {} chunks, hash {}", AppSetting.LOG_SEPARATOR, documentScope.filename(), chunks.size(), hash.substring(0, 8));
        return new LoadedMarkdown(baseMeta, cleaned, chunks);
    }

}