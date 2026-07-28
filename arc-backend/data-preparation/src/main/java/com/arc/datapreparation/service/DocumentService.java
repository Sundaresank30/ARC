package com.arc.datapreparation.service;

import com.arc.datapreparation.dto.DocumentResponse;
import com.arc.datapreparation.entity.Batch;
import com.arc.datapreparation.entity.UploadedDocument;
import com.arc.datapreparation.repository.BatchRepository;
import com.arc.datapreparation.repository.UploadedDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final UploadedDocumentRepository documentRepository;
    private final BatchRepository batchRepository;

    @Value("${document.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv",
            "text/plain"
    );

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, String batchId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 50MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "File type not allowed: " + file.getContentType() + ". Allowed: PDF, Excel, CSV, TXT");
        }

        Batch batch = null;
        if (batchId != null && !batchId.isBlank()) {
            batch = batchRepository.findByBatchId(batchId)
                    .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            UploadedDocument doc = UploadedDocument.builder()
                    .filename(uniqueFilename)
                    .originalFilename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .storagePath(targetPath.toString())
                    .batch(batch)
                    .build();

            UploadedDocument saved = documentRepository.save(doc);
            log.info("Document uploaded: {} (id={})", saved.getOriginalFilename(), saved.getId());
            return DocumentResponse.fromEntity(saved);

        } catch (IOException e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store document. Please try again.");
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByBatch(Long batchId) {
        return documentRepository.findByBatchId(batchId).stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
