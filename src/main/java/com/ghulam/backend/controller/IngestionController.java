package com.ghulam.backend.controller;

import com.ghulam.backend.dtos.IngestResponse;
import com.ghulam.backend.helper.AppSetting;
import com.ghulam.backend.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        var res = ingestionService.ingestUpload(file);
        IngestResponse response = new IngestResponse(res.fileName(), res.status(), res.chunksCount(), res.fileHash());
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/bulk/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<IngestResponse>> uploadBulk(@RequestParam("files") List<MultipartFile> files) throws Exception {
        List<IngestResponse> out = new ArrayList<>();
        for (MultipartFile f : files) {
            var res = ingestionService.ingestUpload(f);
            out.add(new IngestResponse(res.fileName(), res.status(), res.chunksCount(), res.fileHash()));
        }
        return ResponseEntity.ok(out);
    }

    @PostMapping("/directory")
    public ResponseEntity<?> ingestDir(@RequestParam(value = "path", required = false) String path) throws Exception {
        String dirPath = (path == null || path.isBlank()) ? AppSetting.SOURCE_DIR : path;
        Path dir = Paths.get(dirPath);
        var bulk = ingestionService.ingestDirectory(dir);
        return ResponseEntity.ok(bulk);
    }
}