package com.ghulam.backend.helper;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class MarkdownCleaner {

    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\s*\\n.*?\\n---\\s*\\n", Pattern.DOTALL);
    private static final Pattern MULTI_NEWLINE = Pattern.compile("\\n{3,}");

    public String clean(String raw) {
        if (raw == null) return "";
        String noFrontmatter = FRONTMATTER_PATTERN.matcher(raw).replaceFirst("");
        String normalized = MULTI_NEWLINE.matcher(noFrontmatter).replaceAll("\n\n");
        return normalized.trim();
    }

    public Map<String, Object> extractFrontmatter(String raw) {
        var matcher = FRONTMATTER_PATTERN.matcher(raw);
        if (!matcher.find()) return Collections.emptyMap();

        String fmBlock = matcher.group().replaceAll("^---\\s*\\n|\\n---\\s*\\n$", "");
        Map<String, Object> map = new HashMap<>();

        for (String line : fmBlock.split("\n")) {
            if (line.contains(":")) {
                String[] kv = line.split(":", 2);
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }
}