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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarkdownDocumentLoader {

    private final MarkdownValidator validator;
    private final MarkdownCleaner cleaner;
    private final MarkdownChunker chunker;

    public LoadedMarkdown loadFromPath(Path path) throws IOException {
        validator.validateFile(path);
        String raw = Files.readString(path, StandardCharsets.UTF_8);
        return process(raw, path.getFileName().toString());
    }

    public LoadedMarkdown loadFromUpload(MultipartFile file) throws IOException {
        validator.validateUpload(file);
        String raw = new String(file.getBytes(), StandardCharsets.UTF_8);
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.md";
        return process(raw, name);
    }

    private LoadedMarkdown process(String raw, String filename) {
        if (raw == null || raw.isBlank()) throw new RuntimeException("Empty markdown");

        Map<String, Object> fm = cleaner.extractFrontmatter(raw);
        String cleaned = cleaner.clean(raw);
        String hash = DigestUtils.md5DigestAsHex(cleaned.getBytes(StandardCharsets.UTF_8));
        DocumentScope scope = new DocumentScope("workspace", "userId", filename); // TODO

        MarkdownMetadata baseMeta = MarkdownMetadata.builder()
                .documentScope(scope).fileHash(hash)
                .fileSize(cleaned.length()).frontmatter(fm).build();

        List<Document> chunks = chunker.chunk(cleaned, baseMeta);

        log.info("{} Loaded MD {} -> {} chunks, hash {}", AppSetting.LOG_SEPARATOR, filename, chunks.size(), hash.substring(0, 8));
        return new LoadedMarkdown(baseMeta, cleaned, chunks);
    }

}