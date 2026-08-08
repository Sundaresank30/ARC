package com.arc.dashboard.service;

import com.arc.dashboard.dto.CarryForwardDTO;
import com.arc.dashboard.dto.DashboardResponseDTO;
import com.arc.dashboard.dto.LeakageFailureDTO;
import com.arc.dashboard.entity.CarryForwardEmbossing;
import com.arc.dashboard.entity.LeakageFailure;
import com.arc.dashboard.repository.CarryForwardEmbossingRepository;
import com.arc.dashboard.repository.LeakageFailureRepository;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import com.arc.embossing.enums.EmbossingStatus;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.leakagetesting.entity.FailedLeakageTestResult;
import com.arc.leakagetesting.repository.FailedLeakageTestResultRepository;
import com.arc.leakagetesting.repository.LeakageTestResultRepository;
import com.arc.machine.entity.EmbossingQueue;
import com.arc.machine.entity.EmbossingQueueStatus;
import com.arc.machine.repository.EmbossingQueueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CarryForwardEmbossingRepository carryForwardRepository;

    @Mock
    private LeakageFailureRepository leakageFailureRepository;

    @Mock
    private ProductionBatchRepository productionBatchRepository;

    @Mock
    private EmbossingJobRepository embossingJobRepository;

    @Mock
    private LeakageTestResultRepository leakageTestResultRepository;

    @Mock
    private EmbossingQueueRepository embossingQueueRepository;

    @Mock
    private FailedLeakageTestResultRepository failedLeakageTestResultRepository;

    @Mock
    private ProductionBatchItemRepository productionBatchItemRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                carryForwardRepository,
                leakageFailureRepository,
                productionBatchRepository,
                embossingJobRepository,
                leakageTestResultRepository,
                embossingQueueRepository,
                failedLeakageTestResultRepository,
                productionBatchItemRepository
        );
    }

    @Test
    @DisplayName("syncCarryForwardFromEmbossingQueue saves pending items and removes completed items")
    void testSyncCarryForwardFromEmbossingQueue() {
        EmbossingQueue pendingItem = EmbossingQueue.builder()
                .id(1L)
                .partNumber("PN01")
                .serialNumber("P01")
                .status(EmbossingQueueStatus.WAITING)
                .build();

        EmbossingQueue completedItem = EmbossingQueue.builder()
                .id(2L)
                .partNumber("PN02")
                .serialNumber("P02")
                .status(EmbossingQueueStatus.COMPLETED)
                .build();

        when(embossingQueueRepository.findAll()).thenReturn(List.of(pendingItem, completedItem));
        when(carryForwardRepository.findByPartNoAndSerialNo("PN01", "P01")).thenReturn(Optional.empty());

        CarryForwardEmbossing existingCompleted = CarryForwardEmbossing.builder()
                .id(99L)
                .partNo("PN02")
                .serialNo("P02")
                .status("Pending")
                .build();
        when(carryForwardRepository.findByPartNoAndSerialNo("PN02", "P02")).thenReturn(Optional.of(existingCompleted));

        dashboardService.syncCarryForwardFromEmbossingQueue();

        verify(carryForwardRepository, times(1)).save(any(CarryForwardEmbossing.class));
        verify(carryForwardRepository, times(1)).delete(existingCompleted);
    }

    @Test
    @DisplayName("syncLeakageFailuresFromTestResults saves failed leakage results into leakage_failures without duplicates")
    void testSyncLeakageFailuresFromTestResults() {
        FailedLeakageTestResult fResult = FailedLeakageTestResult.builder()
                .id(10L)
                .batchId("Batch_1")
                .partNumber("PN01")
                .serialNumber("P01")
                .pressureValue(83.4)
                .direction("down")
                .attempt("1/2")
                .action("Pending")
                .testedAt(LocalDateTime.now())
                .build();

        when(failedLeakageTestResultRepository.findAll()).thenReturn(List.of(fResult));
        when(leakageFailureRepository.findByPartNoAndSerialNo("PN01", "P01")).thenReturn(Optional.empty());

        dashboardService.syncLeakageFailuresFromTestResults();

        verify(leakageFailureRepository, times(1)).save(any(LeakageFailure.class));
    }

    @Test
    @DisplayName("getDashboardSummary calculates dynamic counts correctly and returns DTO")
    void testGetDashboardSummary() {
        when(embossingQueueRepository.findAll()).thenReturn(Collections.emptyList());
        when(failedLeakageTestResultRepository.findAll()).thenReturn(Collections.emptyList());

        CarryForwardEmbossing cf = CarryForwardEmbossing.builder()
                .id(1L)
                .partNo("PN01")
                .serialNo("P01")
                .status("Pending")
                .remainingSince("12:00, 08 Aug")
                .nextShift("Shift A")
                .action("Pending")
                .build();
        when(carryForwardRepository.findByStatusNotIgnoreCase("Completed")).thenReturn(List.of(cf));

        LeakageFailure lf = LeakageFailure.builder()
                .id(2L)
                .partNo("PN03")
                .serialNo("P03")
                .status("Failed")
                .testValue(85.0)
                .direction("down")
                .timestamp("12:10, 08 Aug")
                .attempt("1/2")
                .action("Pending")
                .build();
        when(leakageFailureRepository.findAll()).thenReturn(List.of(lf));

        when(embossingJobRepository.countByEmbossingStatus(EmbossingStatus.COMPLETED)).thenReturn(5L);
        when(failedLeakageTestResultRepository.count()).thenReturn(1L);
        when(productionBatchRepository.count()).thenReturn(3L);

        DashboardResponseDTO response = dashboardService.getDashboardSummary();

        assertThat(response.getCompletedCount()).isEqualTo(5);
        assertThat(response.getFailedCount()).isEqualTo(1);
        assertThat(response.getTotalBatches()).isEqualTo(3);
        assertThat(response.getCarryForwardEmbossing()).hasSize(1);
        assertThat(response.getCarryForwardEmbossing().get(0).getPartNo()).isEqualTo("PN01");
        assertThat(response.getLeakageTestingFailures()).hasSize(1);
        assertThat(response.getLeakageTestingFailures().get(0).getPartNo()).isEqualTo("PN03");
    }

    @Test
    @DisplayName("resolveCarryForward deletes record by ID")
    void testResolveCarryForward() {
        CarryForwardEmbossing cf = CarryForwardEmbossing.builder().id(5L).partNo("PN05").serialNo("P05").build();
        when(carryForwardRepository.findById(5L)).thenReturn(Optional.of(cf));

        dashboardService.resolveCarryForward(5L);

        verify(carryForwardRepository, times(1)).delete(cf);
    }
}
