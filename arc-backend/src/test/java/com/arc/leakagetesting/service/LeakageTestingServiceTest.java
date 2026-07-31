package com.arc.leakagetesting.service;

import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.enums.MachineStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.dto.LeakageTestItemDto;
import com.arc.leakagetesting.dto.LeakageTestingResponseDto;
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

    @InjectMocks
    private LeakageTestingService leakageTestingService;

    @Test
    @DisplayName("getDashboardData should return 0 failedCount and empty failures when all jobs are COMPLETED")
    void testGetDashboardData_AllCompleted() {
        when(simulationProperties.getActiveBatch()).thenReturn("Batch_1");

        EmbossingJob completed1 = EmbossingJob.builder()
                .id(1L)
                .batchId("Batch_1")
                .partNumber("Pn00001c")
                .serialNumber("P0010001")
                .embossingStatus(EmbossingStatus.COMPLETED)
                .createdTime(LocalDateTime.now())
                .machineStatus(MachineStatus.IDLE)
                .build();

        when(embossingJobRepository.findByBatchIdOrderByIdAsc("Batch_1"))
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
        when(simulationProperties.getActiveBatch()).thenReturn("Batch_1");

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
                .testValue(0.42)
                .direction("down")
                .attempt("2/2")
                .action("Scrap")
                .createdTime(LocalDateTime.now())
                .machineStatus(MachineStatus.IDLE)
                .build();

        when(embossingJobRepository.findByBatchIdOrderByIdAsc("Batch_1"))
                .thenReturn(Arrays.asList(completed, failed));

        LeakageTestingResponseDto response = leakageTestingService.getDashboardData();

        assertThat(response.getFailedCount()).isEqualTo(1);
        assertThat(response.getFailures()).hasSize(1);
        assertThat(response.getFailures().get(0).getPartNo()).isEqualTo("Pn00111c");
        assertThat(response.getFailures().get(0).getAction()).isEqualTo("Scrap");
    }

    @Test
    @DisplayName("updateJobAction should modify job action in repository")
    void testUpdateJobAction() {
        EmbossingJob failed = EmbossingJob.builder()
                .id(1L)
                .partNumber("Pn00111c")
                .serialNumber("P0011156")
                .embossingStatus(EmbossingStatus.FAILED)
                .action("Pending")
                .build();

        when(embossingJobRepository.findById(1L)).thenReturn(Optional.of(failed));
        when(embossingJobRepository.save(any(EmbossingJob.class))).thenAnswer(i -> i.getArgument(0));

        LeakageTestItemDto result = leakageTestingService.updateJobAction(1L, "Scrap");

        assertThat(result.getAction()).isEqualTo("Scrap");
    }
}
