package com.arc.datapreparation.controller;

import com.arc.datapreparation.dto.BatchResponse;
import com.arc.datapreparation.dto.CreateBatchRequest;
import com.arc.datapreparation.dto.UpdateBatchStatusRequest;
import com.arc.datapreparation.entity.BatchStatus;
import com.arc.datapreparation.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-preparation/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping
    public ResponseEntity<BatchResponse> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.createBatch(request));
    }

    @GetMapping
    public ResponseEntity<List<BatchResponse>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatchById(id));
    }

    @GetMapping("/by-batch-id/{batchId}")
    public ResponseEntity<BatchResponse> getBatchByBatchId(@PathVariable String batchId) {
        return ResponseEntity.ok(batchService.getBatchByBatchId(batchId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BatchResponse>> getBatchesByStatus(@PathVariable BatchStatus status) {
        return ResponseEntity.ok(batchService.getBatchesByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BatchResponse> updateBatchStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBatchStatusRequest request) {
        return ResponseEntity.ok(batchService.updateBatchStatus(id, request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
