package com.arc.embossing.service;

import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.entity.EmbossingJob;
import com.arc.embossing.repository.EmbossingJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbossingDataInitializerTest {

    @Mock
    private EmbossingJobRepository embossingJobRepository;

    @Mock
    private ProductionBatchItemRepository productionBatchItemRepository;

    @Mock
    private EmbossingSimulationProperties simulationProperties;

    @InjectMocks
    private EmbossingDataInitializer initializer;

    @Test
    void syncCreatesPendingJobsForPreparedItemsWithoutDuplicates() {
        ProductionBatchItem item = ProductionBatchItem.builder()
                .partNumber("PN-001")
                .serialNumber("SN-001")
                .status("PREPARED")
                .build();

        when(productionBatchItemRepository.findAll()).thenReturn(List.of(item));
        when(embossingJobRepository.existsByPartNumber("PN-001")).thenReturn(false);
        when(embossingJobRepository.existsBySerialNumber("SN-001")).thenReturn(false);
        when(embossingJobRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        initializer.syncEmbossingJobsFromProductionItems();

        verify(embossingJobRepository).saveAll(argThat(jobs -> {
            List<EmbossingJob> jobList = (List<EmbossingJob>) jobs;
            return jobList.size() == 1
                    && jobList.get(0).getPartNumber().equals("PN-001")
                    && jobList.get(0).getSerialNumber().equals("SN-001");
        }));
    }
}
