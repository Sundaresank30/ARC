package com.arc.embossing.service;

import com.arc.dashboard.repository.CarryForwardEmbossingRepository;
import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.machine.entity.EmbossingQueue;
import com.arc.machine.entity.EmbossingQueueStatus;
import com.arc.machine.repository.EmbossingQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmbossingJobStateService {

    private final EmbossingJobRepository embossingJobRepository;
    private final ProductionBatchItemRepository productionBatchItemRepository;
    private final CarryForwardEmbossingRepository carryForwardRepository;
    private final EmbossingQueueRepository embossingQueueRepository;

    public EmbossingJobStateService(
            EmbossingJobRepository embossingJobRepository,
            ProductionBatchItemRepository productionBatchItemRepository,
            @Autowired(required = false) CarryForwardEmbossingRepository carryForwardRepository,
            @Autowired(required = false) EmbossingQueueRepository embossingQueueRepository) {
        this.embossingJobRepository = embossingJobRepository;
        this.productionBatchItemRepository = productionBatchItemRepository;
        this.carryForwardRepository = carryForwardRepository;
        this.embossingQueueRepository = embossingQueueRepository;
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
        updateEmbossingQueueAndCarryForward(job.getPartNumber(), job.getSerialNumber());
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

    private void updateEmbossingQueueAndCarryForward(String partNumber, String serialNumber) {
        if (embossingQueueRepository != null) {
            List<EmbossingQueue> queueItems = embossingQueueRepository.findAll().stream()
                    .filter(q -> partNumber.equals(q.getPartNumber()) && serialNumber.equals(q.getSerialNumber()))
                    .toList();

            for (EmbossingQueue qItem : queueItems) {
                if (qItem.getStatus() != EmbossingQueueStatus.COMPLETED) {
                    qItem.setStatus(EmbossingQueueStatus.COMPLETED);
                    qItem.setPrintedAt(LocalDateTime.now());
                    qItem.setPrintedDate(LocalDate.now());
                    embossingQueueRepository.save(qItem);
                }
            }
        }

        if (carryForwardRepository != null) {
            carryForwardRepository.findByPartNoAndSerialNo(partNumber, serialNumber)
                    .ifPresent(carryForwardRepository::delete);
        }
    }
}
