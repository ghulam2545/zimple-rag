package com.ghulam.backend.helper;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.List;

public final class AppSetting {

    public static final List<String> ALLOWED_EXTENSIONS = List.of(".md", ".markdown");
    public static final int MAX_FILE_SIZE_MB = 5;
    public static final int MIN_CHUNK_LENGTH = 100;
    public static final int CHUNK_SIZE = 800;
    public static final int CHUNK_OVERLAP = 150;
    public static final String SOURCE_DIR = "./documents";

    public static final int TOP_K = 5;
    public static final double SIMILARITY_THRESHOLD = 0.1;
    public static final int MAX_QUERY_LENGTH = 600;

    public static final int CACHE_TTL_MINUTES = 60;
    public static final int CHAT_MEMORY_TTL_HOURS = 24;
    public static final String LOG_SEPARATOR = "──────────────────────────────────────────────────────";

    public static Filter.Expression getFilterExpression(String workspace, String userId, String filename) {
        var b = new FilterExpressionBuilder();
        return b
                .and(
                        b.and(
                                b.eq("workspace", workspace),
                                b.eq("user_id", userId)
                        ),
                        b.eq("file_name", filename)
                )
                .build();
    }

}
