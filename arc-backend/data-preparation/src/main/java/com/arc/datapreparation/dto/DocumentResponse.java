package com.arc.datapreparation.dto;

import com.arc.datapreparation.entity.UploadedDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String batchId;

    public static DocumentResponse fromEntity(UploadedDocument doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .filename(doc.getFilename())
                .originalFilename(doc.getOriginalFilename())
                .contentType(doc.getContentType())
                .fileSize(doc.getFileSize())
                .uploadedAt(doc.getUploadedAt())
                .batchId(doc.getBatch() != null ? doc.getBatch().getBatchId() : null)
                .build();
    }
}
