package com.ghulam.backend.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

@Component
public class MarkdownValidator {

    // Validate a file of {@code Path} type, check extension and size
    public void validateFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        boolean allowed = AppSetting.ALLOWED_EXTENSIONS.stream().anyMatch(name::endsWith);
        if (!allowed)
            throw new RuntimeException("Only MD files allowed: " + AppSetting.ALLOWED_EXTENSIONS);

        File f = path.toFile();
        long maxBytes = (long) AppSetting.MAX_FILE_SIZE_MB * 1024 * 1024;

        if (f.length() > maxBytes)
            throw new RuntimeException("File exceeds max size " + AppSetting.MAX_FILE_SIZE_MB + "MB");
    }

    // Validate a file of {@code MultipartFile} type, check extension and size
    public void validateUpload(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("Empty file");
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";

        boolean allowed = AppSetting.ALLOWED_EXTENSIONS.stream().anyMatch(name::endsWith);
        if (!allowed)
            throw new RuntimeException("Only MD files allowed: " + AppSetting.ALLOWED_EXTENSIONS);

        long maxBytes = (long) AppSetting.MAX_FILE_SIZE_MB * 1024 * 1024;
        if (file.getSize() > maxBytes)
            throw new RuntimeException("File exceeds max size " + AppSetting.MAX_FILE_SIZE_MB + "MB");
    }
}