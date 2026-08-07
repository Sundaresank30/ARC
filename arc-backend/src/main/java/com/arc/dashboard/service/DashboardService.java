package com.arc.dashboard.service;

import com.arc.dashboard.dto.CarryForwardDTO;
import com.arc.dashboard.dto.DashboardResponseDTO;
import com.arc.dashboard.dto.LeakageFailureDTO;
import com.arc.dashboard.entity.CarryForwardEmbossing;
import com.arc.dashboard.entity.LeakageFailure;
import com.arc.dashboard.repository.CarryForwardEmbossingRepository;
import com.arc.dashboard.repository.LeakageFailureRepository;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.repository.LeakageTestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final CarryForwardEmbossingRepository carryForwardRepository;
    private final LeakageFailureRepository leakageFailureRepository;
    private final ProductionBatchRepository productionBatchRepository;
    private final EmbossingJobRepository embossingJobRepository;
    private final LeakageTestResultRepository leakageTestResultRepository;

    @Autowired
    public DashboardService(
            CarryForwardEmbossingRepository carryForwardRepository,
            LeakageFailureRepository leakageFailureRepository,
            @Autowired(required = false) ProductionBatchRepository productionBatchRepository,
            @Autowired(required = false) EmbossingJobRepository embossingJobRepository,
            @Autowired(required = false) LeakageTestResultRepository leakageTestResultRepository) {
        this.carryForwardRepository = carryForwardRepository;
        this.leakageFailureRepository = leakageFailureRepository;
        this.productionBatchRepository = productionBatchRepository;
        this.embossingJobRepository = embossingJobRepository;
        this.leakageTestResultRepository = leakageTestResultRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboardSummary() {
        List<CarryForwardEmbossing> carryForwardList = carryForwardRepository.findAll();
        List<LeakageFailure> leakageFailureList = leakageFailureRepository.findAll();

        long completedJobsCount = (embossingJobRepository != null)
                ? embossingJobRepository.countByEmbossingStatus(EmbossingStatus.COMPLETED)
                : 0;

        long leakageFailuresCount = (leakageTestResultRepository != null)
                ? leakageTestResultRepository.countByStatus("FAILED")
                : leakageFailureList.stream().filter(item -> "Failed".equalsIgnoreCase(item.getStatus())).count();

        long totalBatchesCount = (productionBatchRepository != null)
                ? productionBatchRepository.count()
                : 0;

        List<CarryForwardDTO> carryForwardDTOs = carryForwardList.stream()
                .map(item -> CarryForwardDTO.builder()
                        .id(String.valueOf(item.getId()))
                        .partNo(item.getPartNo())
                        .serialNo(item.getSerialNo())
                        .status(item.getStatus())
                        .remainingSince(item.getRemainingSince())
                        .nextShift(item.getNextShift())
                        .action(item.getAction())
                        .build())
                .collect(Collectors.toList());

        List<LeakageFailureDTO> leakageFailureDTOs = leakageFailureList.stream()
                .map(item -> LeakageFailureDTO.builder()
                        .id(String.valueOf(item.getId()))
                        .partNo(item.getPartNo())
                        .serialNo(item.getSerialNo())
                        .status(item.getStatus())
                        .testValue(item.getTestValue())
                        .direction(item.getDirection())
                        .timestamp(item.getTimestamp())
                        .attempt(item.getAttempt())
                        .action(item.getAction())
                        .build())
                .collect(Collectors.toList());

        return DashboardResponseDTO.builder()
                .completedCount((int) completedJobsCount)
                .failedCount((int) leakageFailuresCount)
                .totalBatches((int) totalBatchesCount)
                .carryForwardEmbossing(carryForwardDTOs)
                .leakageTestingFailures(leakageFailureDTOs)
                .build();
    }

    @Transactional
    public void resolveCarryForward(Long id) {
        carryForwardRepository.findById(id).ifPresent(item -> {
            item.setStatus("Completed");
            carryForwardRepository.save(item);
        });
    }

    @Transactional
    public void resolveLeakageFailure(Long id) {
        leakageFailureRepository.findById(id).ifPresent(item -> {
            item.setStatus("Resolved");
            leakageFailureRepository.save(item);
        });
    }
}
