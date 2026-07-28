package com.arc.datapreparation.service;

import com.arc.datapreparation.dto.BatchResponse;
import com.arc.datapreparation.dto.CreateBatchRequest;
import com.arc.datapreparation.dto.UpdateBatchStatusRequest;
import com.arc.datapreparation.entity.Batch;
import com.arc.datapreparation.entity.BatchStatus;
import com.arc.datapreparation.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository batchRepository;

    @Transactional
    public BatchResponse createBatch(CreateBatchRequest request) {
        if (batchRepository.existsByBatchId(request.getBatchId())) {
            throw new IllegalArgumentException("Batch ID already exists: " + request.getBatchId());
        }

        Batch batch = Batch.builder()
                .batchId(request.getBatchId())
                .partNumberSeries(request.getPartNumberSeries())
                .serialNumberSeries(request.getSerialNumberSeries())
                .totalCount(request.getTotalCount())
                .completedCount(0)
                .failedCount(0)
                .status(BatchStatus.IN_PROGRESS)
                .build();

        Batch saved = batchRepository.save(batch);
        log.info("Batch created: {}", saved.getBatchId());
        return BatchResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getAllBatches() {
        return batchRepository.findAll().stream()
                .map(BatchResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BatchResponse getBatchById(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with id: " + id));
        return BatchResponse.fromEntity(batch);
    }

    @Transactional(readOnly = true)
    public BatchResponse getBatchByBatchId(String batchId) {
        Batch batch = batchRepository.findByBatchId(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with batch_id: " + batchId));
        return BatchResponse.fromEntity(batch);
    }

    @Transactional
    public BatchResponse updateBatchStatus(Long id, UpdateBatchStatusRequest request) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with id: " + id));
        batch.setStatus(request.getStatus());
        Batch updated = batchRepository.save(batch);
        log.info("Batch {} status updated to: {}", updated.getBatchId(), updated.getStatus());
        return BatchResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByStatus(BatchStatus status) {
        return batchRepository.findByStatus(status).stream()
                .map(BatchResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
