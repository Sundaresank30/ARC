package com.arc.dataembossing.service;

import com.arc.dataembossing.dto.*;
import com.arc.dataembossing.entity.Batch;
import com.arc.dataembossing.entity.BatchStatus;
import com.arc.dataembossing.entity.ProductionLog;
import com.arc.dataembossing.repository.BatchRepository;
import com.arc.dataembossing.repository.ProductionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbossingService {

    private final BatchRepository batchRepository;
    private final ProductionLogRepository productionLogRepository;

    @Transactional
    public EmbossingResponse startEmbossing(StartEmbossingRequest request) {
        Batch batch = batchRepository.findByBatchId(request.getBatchId())
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + request.getBatchId()));

        if (batch.getStatus() == BatchStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot start embossing on an already COMPLETED batch");
        }

        batch.setStatus(BatchStatus.EMBOSSING);
        Batch updated = batchRepository.save(batch);

        // Store production start log
        ProductionLog logEntry = ProductionLog.builder()
                .batch(updated)
                .machineCode(request.getMachineCode())
                .productionLineDetails(request.getProductionLineDetails())
                .status("STARTED")
                .logMessage("Embossing process initialized for batch: " + request.getBatchId())
                .timestamp(LocalDateTime.now())
                .build();
        productionLogRepository.save(logEntry);

        log.info("Embossing started for batch {} on machine {}", request.getBatchId(), request.getMachineCode());
        return EmbossingResponse.fromBatch(updated);
    }

    @Transactional
    public EmbossingResponse receiveMachineData(MachineDataRequest request) {
        Batch batch = batchRepository.findByBatchId(request.getBatchId())
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + request.getBatchId()));

        // Validate machine response
        if (request.getMachineCode() == null || request.getMachineCode().isBlank()) {
            throw new IllegalArgumentException("Machine Code cannot be empty");
        }

        // Update progress counts
        if (request.getSuccessIncrement() != null && request.getSuccessIncrement() > 0) {
            batch.setCompletedCount((batch.getCompletedCount() != null ? batch.getCompletedCount() : 0) + request.getSuccessIncrement());
        }
        if (request.getFailureIncrement() != null && request.getFailureIncrement() > 0) {
            batch.setFailedCount((batch.getFailedCount() != null ? batch.getFailedCount() : 0) + request.getFailureIncrement());
        }

        // Auto-complete if progress reaches total
        int totalProcessed = (batch.getCompletedCount() != null ? batch.getCompletedCount() : 0) +
                             (batch.getFailedCount() != null ? batch.getFailedCount() : 0);
        if (batch.getTotalCount() != null && totalProcessed >= batch.getTotalCount()) {
            batch.setStatus(BatchStatus.COMPLETED);
        }

        Batch savedBatch = batchRepository.save(batch);

        // Store embossing log with production line details and timestamp
        ProductionLog logEntry = ProductionLog.builder()
                .batch(savedBatch)
                .machineCode(request.getMachineCode())
                .productionLineDetails(request.getProductionLineDetails())
                .machineResponse(request.getMachineResponse())
                .status(request.getStatus() != null ? request.getStatus() : "PROCESSING")
                .logMessage(request.getLogMessage() != null ? request.getLogMessage() : "Received production data update")
                .timestamp(LocalDateTime.now())
                .build();
        productionLogRepository.save(logEntry);

        log.info("Production telemetry recorded for batch {}: completed={}, failed={}",
                savedBatch.getBatchId(), savedBatch.getCompletedCount(), savedBatch.getFailedCount());

        return EmbossingResponse.fromBatch(savedBatch);
    }

    @Transactional
    public EmbossingResponse completeBatch(String batchId) {
        Batch batch = batchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        batch.setStatus(BatchStatus.COMPLETED);
        Batch completed = batchRepository.save(batch);

        // Record completion log
        ProductionLog logEntry = ProductionLog.builder()
                .batch(completed)
                .status("COMPLETED")
                .logMessage("Production batch " + batchId + " completed successfully.")
                .timestamp(LocalDateTime.now())
                .build();
        productionLogRepository.save(logEntry);

        log.info("Batch {} marked as COMPLETED", batchId);
        return EmbossingResponse.fromBatch(completed);
    }

    @Transactional(readOnly = true)
    public List<ProductionLogResponse> getProductionLogs(Long batchId) {
        return productionLogRepository.findByBatchIdOrderByTimestampDesc(batchId).stream()
                .map(ProductionLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
