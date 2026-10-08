package com.arc.datapreparation.controller;

import com.arc.datapreparation.dto.CreateProductionBatchRequest;
import com.arc.datapreparation.dto.ProductionBatchItemDto;
import com.arc.datapreparation.dto.ProductionBatchResponse;
import com.arc.datapreparation.service.DataPreparationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/data-preparation")
@RequiredArgsConstructor
@Validated
public class DataPreparationController {

    private final DataPreparationService dataPreparationService;

    @GetMapping("/server-date")
    public ResponseEntity<Map<String, String>> getServerDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        String formattedDate = now.format(formatter);
        return ResponseEntity.ok(Map.of(
                "formattedDate", formattedDate,
                "isoDate", now.toString()
        ));
    }

    @PostMapping("/batches")
    public ResponseEntity<ProductionBatchResponse> createProductionBatch(@Valid @RequestBody CreateProductionBatchRequest request) {
        ProductionBatchResponse response = dataPreparationService.createProductionBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/batches")
    public ResponseEntity<List<ProductionBatchResponse>> getAllBatches() {
        return ResponseEntity.ok(dataPreparationService.getAllBatches());
    }

    @GetMapping("/batches/{batchId}")
    public ResponseEntity<ProductionBatchResponse> getBatchByBatchId(
            @PathVariable @Size(max = 100, message = "Batch ID cannot exceed 100 characters") String batchId) {
        return ResponseEntity.ok(dataPreparationService.getBatchByBatchId(batchId));
    }

    @GetMapping("/batches/{batchId}/items")
    public ResponseEntity<List<ProductionBatchItemDto>> getBatchItems(
            @PathVariable @Size(max = 100, message = "Batch ID cannot exceed 100 characters") String batchId) {
        return ResponseEntity.ok(dataPreparationService.getBatchItems(batchId));
    }
}
