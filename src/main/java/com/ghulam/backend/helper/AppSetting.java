package com.ghulam.backend.helper;

import java.util.List;

public final class AppSetting {

    public static final List<String> ALLOWED_EXTENSIONS = List.of(".md", ".markdown");
    public static final int MAX_FILE_SIZE_MB = 5;
    public static final int MIN_CHUNK_LENGTH = 100;
    public static final int CHUNK_SIZE = 800;
    public static final int CHUNK_OVERLAP = 150;

    public static final int CACHE_TTL_MINUTES = 60;
}
