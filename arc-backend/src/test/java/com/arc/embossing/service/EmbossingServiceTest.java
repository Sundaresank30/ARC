package com.arc.embossing.service;

import com.arc.datapreparation.entity.ProductionBatchItem;
import com.arc.embossing.config.EmbossingSimulationProperties;
import com.arc.embossing.dto.BatchProgressResponse;
import com.arc.embossing.mapper.EmbossingJobMapper;
import com.arc.embossing.repository.EmbossingJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EmbossingServiceTest {

    @Mock
    private EmbossingJobRepository embossingJobRepository;

    @Mock
    private EmbossingJobMapper embossingJobMapper;

    @Mock
    private EmbossingSimulationProperties simulationProperties;

    @Test
    void buildBatchProgressCalculatesCompletedPendingAndShiftValues() {
        EmbossingService service = new EmbossingService(
                embossingJobRepository,
                embossingJobMapper,
                simulationProperties,
                null,
                null,
                null
        );

        List<ProductionBatchItem> items = List.of(
                ProductionBatchItem.builder().itemIndex(1).status("COMPLETED").build(),
                ProductionBatchItem.builder().itemIndex(2).status("COMPLETED").build(),
                ProductionBatchItem.builder().itemIndex(3).status("WAITING").build(),
                ProductionBatchItem.builder().itemIndex(4).status("WAITING").build(),
                ProductionBatchItem.builder().itemIndex(5).status("COMPLETED").build(),
                ProductionBatchItem.builder().itemIndex(6).status("WAITING").build(),
                ProductionBatchItem.builder().itemIndex(7).status("WAITING").build(),
                ProductionBatchItem.builder().itemIndex(8).status("WAITING").build(),
                ProductionBatchItem.builder().itemIndex(9).status("COMPLETED").build(),
                ProductionBatchItem.builder().itemIndex(10).status("WAITING").build()
        );

        BatchProgressResponse progress = service.buildBatchProgress("BATCH-01", items);

        assertThat(progress.getBatchId()).isEqualTo("BATCH-01");
        assertThat(progress.getProgressPercent()).isEqualTo(40);
    }
}
