package com.arc.embossing.service;

import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmbossingJobStateService {

    private final EmbossingJobRepository embossingJobRepository;
    private final ProductionBatchItemRepository productionBatchItemRepository;

    public EmbossingJobStateService(
            EmbossingJobRepository embossingJobRepository,
            ProductionBatchItemRepository productionBatchItemRepository) {
        this.embossingJobRepository = embossingJobRepository;
        this.productionBatchItemRepository = productionBatchItemRepository;
    }

    @Transactional
    public void transitionToInMachine(Long jobId) {
        EmbossingJob job = embossingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));

        job.setEmbossingStatus(EmbossingStatus.IN_MACHINE);
        job.setMachineStatus(MachineStatus.IN_MACHINE);
        job.setEmbossingStartTime(LocalDateTime.now());
        embossingJobRepository.save(job);
    }

    @Transactional
    public void transitionToPrinting(Long jobId) {
        EmbossingJob job = embossingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));

        job.setEmbossingStatus(EmbossingStatus.PRINTING);
        job.setMachineStatus(MachineStatus.PRINTING);
        embossingJobRepository.save(job);
    }

    @Transactional
    public void transitionToCompleted(Long jobId) {
        EmbossingJob job = embossingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));

        job.setEmbossingStatus(EmbossingStatus.COMPLETED);
        job.setMachineStatus(MachineStatus.IDLE);
        job.setEmbossingCompletedTime(LocalDateTime.now());
        embossingJobRepository.save(job);

        updateProductionBatchItemStatus(job.getPartNumber(), job.getSerialNumber());
    }

    private void updateProductionBatchItemStatus(String partNumber, String serialNumber) {
        List<ProductionBatchItem> items = productionBatchItemRepository.findAll().stream()
                .filter(item -> partNumber.equals(item.getPartNumber()) && serialNumber.equals(item.getSerialNumber()))
                .toList();

        for (ProductionBatchItem item : items) {
            item.setStatus("COMPLETED");
        }

        if (!items.isEmpty()) {
            productionBatchItemRepository.saveAll(items);
        }
    }
}
