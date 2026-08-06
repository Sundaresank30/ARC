package com.arc.embossing.service;

import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.repository.EmbossingJobRepository;
import com.arc.machine.entity.EmbossingQueue;
import com.arc.machine.entity.EmbossingQueueStatus;
import com.arc.machine.repository.EmbossingQueueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbossingDataInitializerTest {

    @Mock
    private EmbossingJobRepository embossingJobRepository;

    @Mock
    private ProductionBatchItemRepository productionBatchItemRepository;

    @Mock
    private EmbossingQueueRepository embossingQueueRepository;

    @Mock
    private EmbossingSimulationProperties simulationProperties;

    @InjectMocks
    private EmbossingDataInitializer initializer;

    @Test
    void syncJobsFromQueueSavesJobForQueueItems() {
        EmbossingQueue queueItem = EmbossingQueue.builder()
                .id(1L)
                .partNumber("PN-001")
                .serialNumber("SN-001")
                .status(EmbossingQueueStatus.WAITING)
                .build();

        when(embossingQueueRepository.findAll()).thenReturn(List.of(queueItem));
        when(embossingJobRepository.findBySerialNumberAndPartNumber("SN-001", "PN-001")).thenReturn(List.of());

        initializer.syncEmbossingJobsFromQueue();

        verify(embossingJobRepository).save(any());
    }
}
