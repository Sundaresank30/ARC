package com.arc.embossing.service;

import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.dto.CurrentMachineResponse;
import com.arc.embossing.dto.EmbossingDashboardResponse;
import com.arc.embossing.dto.EmbossingJobResponse;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.mapper.EmbossingJobMapper;
import com.arc.embossing.repository.EmbossingJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class EmbossingService {

    private final EmbossingJobRepository embossingJobRepository;
    private final EmbossingJobMapper embossingJobMapper;
    private final EmbossingSimulationProperties simulationProperties;

    public EmbossingService(
            EmbossingJobRepository embossingJobRepository,
            EmbossingJobMapper embossingJobMapper,
            EmbossingSimulationProperties simulationProperties) {
        this.embossingJobRepository = embossingJobRepository;
        this.embossingJobMapper = embossingJobMapper;
        this.simulationProperties = simulationProperties;
    }

    @Transactional(readOnly = true)
    public EmbossingDashboardResponse getDashboard() {
        String activeBatch = simulationProperties.getActiveBatch();
        long pendingCount = embossingJobRepository.countByBatchIdAndEmbossingStatus(
                activeBatch, EmbossingStatus.PENDING);
        List<EmbossingJob> jobs = embossingJobRepository.findByBatchIdOrderByIdAsc(activeBatch);

        return EmbossingDashboardResponse.builder()
                .activeBatch(activeBatch)
                .pendingCount(pendingCount)
                .jobs(embossingJobMapper.toResponseList(jobs))
                .build();
    }

    @Transactional(readOnly = true)
    public CurrentMachineResponse getCurrentMachineJob() {
        return embossingJobRepository
                .findFirstByEmbossingStatusInOrderByIdAsc(
                        List.of(EmbossingStatus.IN_MACHINE, EmbossingStatus.PRINTING))
                .map(this::toCurrentMachineResponse)
                .orElseGet(() -> CurrentMachineResponse.builder()
                        .partNumber(null)
                        .serialNumber(null)
                        .machineStatus(MachineStatus.WAITING)
                        .build());
    }

    @Transactional(readOnly = true)
    public List<EmbossingJobResponse> getPendingJobs() {
        List<EmbossingJob> pendingJobs = embossingJobRepository
                .findByEmbossingStatusOrderByIdAsc(EmbossingStatus.PENDING);
        return embossingJobMapper.toResponseList(pendingJobs);
    }

    @Transactional(readOnly = true)
    public List<EmbossingJobResponse> getCompletedJobs() {
        List<EmbossingJob> completedJobs = embossingJobRepository
                .findByEmbossingStatusOrderByIdAsc(EmbossingStatus.COMPLETED);
        return embossingJobMapper.toResponseList(completedJobs);
    }

    private CurrentMachineResponse toCurrentMachineResponse(EmbossingJob job) {
        MachineStatus displayStatus = job.getEmbossingStatus() == EmbossingStatus.IN_MACHINE
                ? MachineStatus.IN_MACHINE
                : MachineStatus.PRINTING;

        return CurrentMachineResponse.builder()
                .partNumber(job.getPartNumber())
                .serialNumber(job.getSerialNumber())
                .machineStatus(displayStatus)
                .build();
    }
}
