package com.ghulam.backend.helper;

import com.ghulam.backend.dtos.MarkdownMetadata;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MarkdownChunker {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)$", Pattern.MULTILINE);
    private static final Pattern CODEBLOCK_PATTERN = Pattern.compile("```[\\s\\S]*?```");

    public List<Document> chunk(String cleanedContent, MarkdownMetadata baseMeta) {
        List<Section> sections = splitByHeadings(cleanedContent);
        List<Document> docs = new ArrayList<>();
        Deque<String> headingStack = new ArrayDeque<>();
        for (Section sec : sections) {
            updateHeadingStack(headingStack, sec.headingLevel, sec.headingText);
            List<String> chunks = slidingWindowChunk(sec.content);
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                if (chunkText.length() < AppSetting.MIN_CHUNK_LENGTH) continue;
                Map<String, Object> meta = new HashMap<>();
                meta.put("workspace", baseMeta.getWorkspace());
                meta.put("user_id", baseMeta.getUserId());
                meta.put("file_path", baseMeta.getFilePath());
                meta.put("file_name", baseMeta.getFileName());
                meta.put("file_hash", baseMeta.getFileHash());
                meta.put("heading", sec.headingText);
                meta.put("heading_level", sec.headingLevel);
                meta.put("headings_hierarchy", String.join(" > ", headingStack));
                meta.put("chunk_index", i);
                meta.put("total_chunks", chunks.size());
                meta.put("frontmatter", baseMeta.getFrontmatter());
                String slug = sec.headingText.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9\\-]", "");
                meta.put("source", baseMeta.getFilePath() + (sec.headingText.isEmpty() ? "" : "#" + slug));
                docs.add(new Document(chunkText, meta));
            }
        }
        return docs;
    }

    private List<Section> splitByHeadings(String content) {
        List<Section> sections = new ArrayList<>();
        Matcher m = HEADING_PATTERN.matcher(content);
        List<Integer> indices = new ArrayList<>();
        List<String> headings = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        while (m.find()) {
            indices.add(m.start());
            headings.add(m.group(2).trim());
            levels.add(m.group(1).length());
        }
        indices.add(content.length());
        if (headings.isEmpty()) {
            sections.add(new Section(0, "", 0, content));
            return sections;
        }
        if (indices.get(0) > 0) {
            sections.add(new Section(0, "", 0, content.substring(0, indices.get(0))));
        }
        for (int i = 0; i < headings.size(); i++) {
            int start = indices.get(i);
            int end = indices.get(i + 1);
            String secContent = content.substring(start, end);
            sections.add(new Section(levels.get(i), headings.get(i), i, secContent));
        }
        return sections;
    }

    private List<String> slidingWindowChunk(String text) {
        List<String> codeBlocks = new ArrayList<>();
        Matcher cbMatcher = CODEBLOCK_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (cbMatcher.find()) {
            codeBlocks.add(cbMatcher.group());
            cbMatcher.appendReplacement(sb, "§CODEBLOCK_" + (codeBlocks.size() - 1) + "§");
        }
        cbMatcher.appendTail(sb);
        String placeholderText = sb.toString();
        int chunkSize = AppSetting.CHUNK_SIZE;
        int overlap = AppSetting.CHUNK_OVERLAP;
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = placeholderText.split("\n\n");
        StringBuilder current = new StringBuilder();
        for (String para : paragraphs) {
            if (current.length() + para.length() > chunkSize && !current.isEmpty()) {
                chunks.add(restoreCodeBlocks(current.toString(), codeBlocks));
                String overlapText = current.length() > overlap ? current.substring(current.length() - overlap) : current.toString();
                current = new StringBuilder(overlapText + "\n\n" + para);
            } else {
                if (!current.isEmpty()) current.append("\n\n");
                current.append(para);
            }
        }
        if (!current.isEmpty()) chunks.add(restoreCodeBlocks(current.toString(), codeBlocks));
        return chunks;
    }

    private String restoreCodeBlocks(String text, List<String> blocks) {
        String restored = text;
        for (int i = 0; i < blocks.size(); i++) {
            restored = restored.replace("§CODEBLOCK_" + i + "§", blocks.get(i));
        }
        return restored;
    }

    private void updateHeadingStack(Deque<String> stack, int level, String heading) {
        while (stack.size() >= level) stack.pollLast();
        if (!heading.isEmpty()) stack.addLast(heading);
    }

    private record Section(int headingLevel, String headingText, int index, String content) {
    }
}