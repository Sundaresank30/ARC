package com.arc.datapreparation.controller;

import com.arc.datapreparation.dto.SourceDocumentDto;
import com.arc.datapreparation.service.SourceDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/source-documents")
@RequiredArgsConstructor
public class SourceDocumentController {

    private final SourceDocumentService sourceDocumentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SourceDocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "batchId", required = false) String batchId) throws IOException {
        SourceDocumentDto response = sourceDocumentService.processAndSavePdf(file, batchId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SourceDocumentDto>> getAllSourceDocuments() {
        return ResponseEntity.ok(sourceDocumentService.getAllSourceDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceDocumentDto> getSourceDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(sourceDocumentService.getSourceDocumentById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSourceDocument(@PathVariable Long id) {
        sourceDocumentService.deleteSourceDocument(id);
        return ResponseEntity.noContent().build();
    }
}
