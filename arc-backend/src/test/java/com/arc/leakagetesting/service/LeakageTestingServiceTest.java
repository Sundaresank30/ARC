package com.arc.leakagetesting.service;

import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.dto.LeakageTestItemDto;
import com.arc.leakagetesting.dto.LeakageTestingResponseDto;
import com.arc.leakagetesting.repository.FailedLeakageTestResultRepository;
import com.arc.leakagetesting.repository.LeakageTestResultRepository;
import com.arc.leakagetesting.repository.PassedLeakageTestResultRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeakageTestingServiceTest {

    @Mock
    private EmbossingJobRepository embossingJobRepository;

    @Mock
    private EmbossingSimulationProperties simulationProperties;

    @Mock
    private LeakageTestResultRepository resultRepository;

    @Mock
    private PassedLeakageTestResultRepository passedResultRepository;

    @Mock
    private FailedLeakageTestResultRepository failedResultRepository;

    @InjectMocks
    private LeakageTestingService leakageTestingService;

    @Test
    @DisplayName("getDashboardData should return 0 failedCount and empty failures when all jobs are COMPLETED")
    void testGetDashboardData_AllCompleted() {
        EmbossingJob completed1 = EmbossingJob.builder()
                .id(1L)
                .batchId("Batch_1")
                .partNumber("Pn00001c")
                .serialNumber("P0010001")
                .embossingStatus(EmbossingStatus.COMPLETED)
                .createdTime(LocalDateTime.now())
                .machineStatus(MachineStatus.IDLE)
                .build();

        when(embossingJobRepository.findByEmbossingStatusOrderByIdDesc(EmbossingStatus.COMPLETED))
                .thenReturn(Collections.singletonList(completed1));

        LeakageTestingResponseDto response = leakageTestingService.getDashboardData();

        assertThat(response.getFailedCount()).isEqualTo(0);
        assertThat(response.getFailures()).isEmpty();
        assertThat(response.getBatchProgressPercent()).isEqualTo(100);
        assertThat(response.getActiveBatch()).isEqualTo("Batch_1");
    }

    @Test
    @DisplayName("getDashboardData should filter and populate only FAILED jobs into failures list")
    void testGetDashboardData_WithFailedJobs() {
        EmbossingJob completed = EmbossingJob.builder()
                .id(1L)
                .batchId("Batch_1")
                .partNumber("Pn00001c")
                .serialNumber("P0010001")
                .embossingStatus(EmbossingStatus.COMPLETED)
                .createdTime(LocalDateTime.now())
                .machineStatus(MachineStatus.IDLE)
                .build();

        EmbossingJob failed = EmbossingJob.builder()
                .id(2L)
                .batchId("Batch_1")
                .partNumber("Pn00111c")
                .serialNumber("P0011156")
                .embossingStatus(EmbossingStatus.FAILED)
                .createdTime(LocalDateTime.now())
                .machineStatus(MachineStatus.IDLE)
                .build();

        when(embossingJobRepository.findByEmbossingStatusOrderByIdDesc(EmbossingStatus.COMPLETED))
                .thenReturn(Collections.singletonList(completed));
        when(embossingJobRepository.findByBatchIdOrderByIdAsc("Batch_1"))
                .thenReturn(Arrays.asList(completed, failed));

        LeakageTestingResponseDto response = leakageTestingService.getDashboardData();

        assertThat(response.getFailedCount()).isEqualTo(1);
        assertThat(response.getFailures()).hasSize(1);
        assertThat(response.getFailures().get(0).getPartNo()).isEqualTo("Pn00111c");
    }

    @Test
    @DisplayName("updateJobAction should handle job action update request")
    void testUpdateJobAction() {
        EmbossingJob failed = EmbossingJob.builder()
                .id(1L)
                .batchId("Batch_1")
                .partNumber("Pn00111c")
                .serialNumber("P0011156")
                .embossingStatus(EmbossingStatus.FAILED)
                .build();

        when(embossingJobRepository.findById(1L)).thenReturn(Optional.of(failed));

        LeakageTestItemDto result = leakageTestingService.updateJobAction(1L, "Scrap");

        assertThat(result.getPartNo()).isEqualTo("Pn00111c");
    }
}
