package com.ghulam.backend.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class MarkdownMetadata {
    @Builder.Default
    private String workspace = "workspace";
    @Builder.Default
    private String userId = "userId";
    private String filename;
    private String fileHash;
    private long fileSize;
    private List<String> headingsHierarchy;
    private String currentHeading;
    private int headingLevel;
    private Map<String, Object> frontmatter;
    private int chunkIndex;
    private int totalChunks;
}