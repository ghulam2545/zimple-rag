package com.ghulam.backend.controller;

import com.ghulam.backend.dtos.DocumentScope;
import com.ghulam.backend.dtos.IngestResponse;
import com.ghulam.backend.service.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @Operation(
            summary = "Upload a file with JSON",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "file", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE),
                                    @Encoding(name = "documentScope", contentType = MediaType.APPLICATION_JSON_VALUE)
                            }
                    )
            )
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestResponse> upload(@RequestPart("file") MultipartFile file, @RequestPart("documentScope") @Valid DocumentScope documentScope) throws Exception {
        var res = ingestionService.ingestUploadedFile(file, documentScope);
        IngestResponse response = new IngestResponse(res.fileName(), res.status(), res.chunksCount(), res.fileHash());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Upload multiple files with JSON",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE),
                                    @Encoding(name = "documentScope", contentType = MediaType.APPLICATION_JSON_VALUE)
                            }
                    )
            )
    )
    @PostMapping(path = "/bulk/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<IngestResponse>> uploadBulk(@RequestPart("files") List<MultipartFile> files, @RequestPart("documentScope") @Valid DocumentScope documentScope) throws Exception {
        List<IngestResponse> out = new ArrayList<>();
        for (MultipartFile f : files) {
            var res = ingestionService.ingestUploadedFile(f, documentScope);
            out.add(new IngestResponse(res.fileName(), res.status(), res.chunksCount(), res.fileHash()));
        }
        return ResponseEntity.ok(out);
    }

}