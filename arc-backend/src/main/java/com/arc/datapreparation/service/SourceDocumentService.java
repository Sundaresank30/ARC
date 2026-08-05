package com.arc.datapreparation.service;

import com.arc.datapreparation.dto.SourceDocumentDto;
import com.arc.datapreparation.entity.SourceDocument;
import com.arc.datapreparation.repository.SourceDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourceDocumentService {

    private final SourceDocumentRepository repository;
    private final PdfExtractionService pdfExtractionService;

    @Transactional
    public SourceDocumentDto processAndSavePdf(MultipartFile file, String batchId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        SourceDocument extracted = pdfExtractionService.extractAndParsePdf(file);
        String finalBatchId = (batchId != null && !batchId.trim().isEmpty()) ? batchId.trim() : extracted.getBatchId();

        SourceDocument documentToSave;
        if (finalBatchId != null) {
            List<SourceDocument> existing = repository.findByBatchId(finalBatchId);
            if (!existing.isEmpty()) {
                documentToSave = existing.get(0);
                if (extracted.getClientName() != null) documentToSave.setClientName(extracted.getClientName());
                if (extracted.getPlant() != null) documentToSave.setPlant(extracted.getPlant());
                if (extracted.getProduct() != null) documentToSave.setProduct(extracted.getProduct());
                if (extracted.getVacuumSetpoint() != null) documentToSave.setVacuumSetpoint(extracted.getVacuumSetpoint());
                if (extracted.getMaximumVacuum() != null) documentToSave.setMaximumVacuum(extracted.getMaximumVacuum());
                if (extracted.getMinimumVacuum() != null) documentToSave.setMinimumVacuum(extracted.getMinimumVacuum());
                if (extracted.getWarningThreshold() != null) documentToSave.setWarningThreshold(extracted.getWarningThreshold());
                if (extracted.getAlarmThreshold() != null) documentToSave.setAlarmThreshold(extracted.getAlarmThreshold());
                if (extracted.getVacuumHoldTime() != null) documentToSave.setVacuumHoldTime(extracted.getVacuumHoldTime());
                if (extracted.getMotorCurrent() != null) documentToSave.setMotorCurrent(extracted.getMotorCurrent());
                if (extracted.getMotorTemperature() != null) documentToSave.setMotorTemperature(extracted.getMotorTemperature());
                if (extracted.getOperatingPressure() != null) documentToSave.setOperatingPressure(extracted.getOperatingPressure());
                if (extracted.getCycleTime() != null) documentToSave.setCycleTime(extracted.getCycleTime());
                documentToSave.setUploadedAt(java.time.LocalDateTime.now());
            } else {
                documentToSave = extracted;
                documentToSave.setBatchId(finalBatchId);
            }
        } else {
            documentToSave = extracted;
        }

        SourceDocument saved = repository.save(documentToSave);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SourceDocumentDto> getAllSourceDocuments() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SourceDocumentDto getSourceDocumentById(Long id) {
        SourceDocument doc = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Source document not found with id: " + id));
        return mapToDto(doc);
    }

    @Transactional
    public void deleteSourceDocument(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Source document not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private SourceDocumentDto mapToDto(SourceDocument doc) {
        return SourceDocumentDto.builder()
                .id(doc.getId())
                .batchId(doc.getBatchId())
                .clientName(doc.getClientName())
                .plant(doc.getPlant())
                .product(doc.getProduct())
                .vacuumSetpoint(doc.getVacuumSetpoint())
                .maximumVacuum(doc.getMaximumVacuum())
                .minimumVacuum(doc.getMinimumVacuum())
                .warningThreshold(doc.getWarningThreshold())
                .alarmThreshold(doc.getAlarmThreshold())
                .vacuumHoldTime(doc.getVacuumHoldTime())
                .motorCurrent(doc.getMotorCurrent())
                .motorTemperature(doc.getMotorTemperature())
                .operatingPressure(doc.getOperatingPressure())
                .cycleTime(doc.getCycleTime())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
