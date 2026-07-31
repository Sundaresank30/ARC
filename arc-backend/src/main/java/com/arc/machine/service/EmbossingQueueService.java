package com.arc.machine.service;

import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.machine.dto.EmbossingQueueDto;
import com.arc.machine.entity.EmbossingQueue;
import com.arc.machine.entity.EmbossingQueueStatus;
import com.arc.machine.repository.EmbossingQueueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Machine Module Queue Engine — DB is single source of truth.
 *
 * Keeps a rolling buffer of up to 5 non-COMPLETED records on the frontend.
 * The old in-memory ConcurrentHashMap MachineService is left untouched for
 * backward-compatibility with existing /api/machine/records endpoints.
 */
@Service
public class EmbossingQueueService {

    private final EmbossingQueueRepository repo;
    private final ProductionBatchItemRepository productionItemRepository;

    public EmbossingQueueService(
            EmbossingQueueRepository repo,
            ProductionBatchItemRepository productionItemRepository) {
        this.repo = repo;
        this.productionItemRepository = productionItemRepository;
    }

    private EmbossingQueue build(String partNumber, String serialNumber) {
        return EmbossingQueue.builder()
                .partNumber(partNumber)
                .serialNumber(serialNumber)
                .status(EmbossingQueueStatus.WAITING)
                .build();
    }

    // -------------------------------------------------------------------------
    // Buffer — top 5 records that are NOT COMPLETED, ordered by id asc
    // -------------------------------------------------------------------------
    @Transactional
    public List<EmbossingQueueDto> getQueueBuffer() {
        synchronizePreparedItems();
        return repo.findTop5ByStatusNotOrderByIdAsc(EmbossingQueueStatus.COMPLETED)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Mark IN_PROGRESS
    // -------------------------------------------------------------------------
    @Transactional
    public EmbossingQueueDto markInProgress(Long id) {
        EmbossingQueue item = repo.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Queue item not found: " + id));
        if (item.getStatus() == EmbossingQueueStatus.WAITING) {
            item.setStatus(EmbossingQueueStatus.IN_PROGRESS);
            return toDto(repo.save(item));
        }
        return toDto(item);
    }

    // -------------------------------------------------------------------------
    // Mark COMPLETED — set printed_at = NOW(), printed_date = CURRENT_DATE
    // -------------------------------------------------------------------------
    @Transactional
    public EmbossingQueueDto markCompleted(Long id) {
        EmbossingQueue item = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Queue item not found: " + id));
        item.setStatus(EmbossingQueueStatus.COMPLETED);
        item.setPrintedAt(LocalDateTime.now());
        item.setPrintedDate(LocalDate.now());
        EmbossingQueue saved = repo.save(item);

        productionItemRepository
                .findBySerialNumberAndPartNumber(saved.getSerialNumber(), saved.getPartNumber())
                .ifPresent(preparedItem -> {
                    if (!"COMPLETED".equalsIgnoreCase(preparedItem.getStatus())) {
                        preparedItem.setStatus("COMPLETED");
                        productionItemRepository.save(preparedItem);
                    }
                });

        return toDto(saved);
    }

    // -------------------------------------------------------------------------
    // Next WAITING — for appending to the rolling buffer after a completion
    // -------------------------------------------------------------------------
    @Transactional
    public Optional<EmbossingQueueDto> fetchNextWaiting() {
        synchronizePreparedItems();
        return repo.findFirstByStatusOrderByIdAsc(EmbossingQueueStatus.WAITING)
                .map(this::toDto);
    }

    // -------------------------------------------------------------------------
    // Atomically claim the next waiting item for processing.
    // This prevents duplicate prints and race conditions across concurrent clients.
    // -------------------------------------------------------------------------
    @Transactional
    public Optional<EmbossingQueueDto> claimNextWaiting() {
        synchronizePreparedItems();
        return repo.claimFirstByStatusOrderByIdAsc(EmbossingQueueStatus.WAITING)
                .map(item -> {
                    item.setStatus(EmbossingQueueStatus.IN_PROGRESS);
                    return toDto(repo.save(item));
                });
    }

    // -------------------------------------------------------------------------
    // Reset — sets all rows back to WAITING, clears timestamps
    // -------------------------------------------------------------------------
    @Transactional
    public List<EmbossingQueueDto> resetAll() {
        repo.resetAllToWaiting();
        // after bulk update, return fresh buffer
        return getQueueBuffer();
    }

    // -------------------------------------------------------------------------
    // Import pending Data Preparation items into the machine queue.
    // -------------------------------------------------------------------------
    private void synchronizePreparedItems() {
        List<ProductionBatchItem> preparedItems = productionItemRepository.findByStatusOrderByIdAsc("PREPARED");
        for (ProductionBatchItem item : preparedItems) {
            if (!repo.existsBySerialNumberAndPartNumber(item.getSerialNumber(), item.getPartNumber())) {
                repo.save(build(item.getPartNumber(), item.getSerialNumber()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Mapper
    // -------------------------------------------------------------------------
    private EmbossingQueueDto toDto(EmbossingQueue e) {
        return EmbossingQueueDto.builder()
                .id(e.getId())
                .partNumber(e.getPartNumber())
                .serialNumber(e.getSerialNumber())
                .status(e.getStatus().name())
                .printedAt(e.getPrintedAt())
                .printedDate(e.getPrintedDate())
                .build();
    }
}
