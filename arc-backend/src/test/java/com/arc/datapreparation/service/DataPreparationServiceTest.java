package com.arc.datapreparation.service;

import com.arc.datapreparation.dto.CreateProductionBatchRequest;
import com.arc.datapreparation.dto.ProductionBatchItemDto;
import com.arc.datapreparation.dto.ProductionBatchResponse;
import com.arc.datapreparation.entity.ProductionBatch;
import com.arc.datapreparation.repository.ProductionBatchItemRepository;
import com.arc.datapreparation.repository.ProductionBatchRepository;
import com.arc.embossing.repository.EmbossingJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataPreparationServiceTest {

    @Mock
    private ProductionBatchRepository batchRepository;

    @Mock
    private ProductionBatchItemRepository itemRepository;

    @Mock
    private EmbossingJobRepository embossingJobRepository;

    @InjectMocks
    private DataPreparationService dataPreparationService;

    private CreateProductionBatchRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = CreateProductionBatchRequest.builder()
                .batchId("batch 4")
                .partNoSeries("PN001")
                .partNoCount(10)
                .serialNoSeries("SN001")
                .serialNoCount(10)
                .build();
    }

    @Test
    @DisplayName("createProductionBatch should successfully save batch and 10 items in database")
    void testCreateProductionBatch_Success() {
        when(batchRepository.existsByBatchId("batch 4")).thenReturn(false);
        when(batchRepository.save(any(ProductionBatch.class))).thenAnswer(invocation -> {
            ProductionBatch b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });

        ProductionBatchResponse response = dataPreparationService.createProductionBatch(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBatchId()).isEqualTo("batch 4");
        assertThat(response.getTotalItems()).isEqualTo(10);
        assertThat(response.getItems()).hasSize(10);

        // Verify item #1
        ProductionBatchItemDto item1 = response.getItems().get(0);
        assertThat(item1.getItemIndex()).isEqualTo(1);
        assertThat(item1.getPartNumber()).isEqualTo("PN001");
        assertThat(item1.getSerialNumber()).isEqualTo("SN001");

        // Verify item #10
        ProductionBatchItemDto item10 = response.getItems().get(9);
        assertThat(item10.getItemIndex()).isEqualTo(10);
        assertThat(item10.getPartNumber()).isEqualTo("PN010");
        assertThat(item10.getSerialNumber()).isEqualTo("SN010");

        verify(batchRepository, times(1)).save(any(ProductionBatch.class));
    }

    @Test
    @DisplayName("createProductionBatch should throw exception when batch ID already exists")
    void testCreateProductionBatch_DuplicateBatchId() {
        when(batchRepository.existsByBatchId("batch 4")).thenReturn(true);

        assertThatThrownBy(() -> dataPreparationService.createProductionBatch(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Production batch with ID 'batch 4' already exists.");

        verify(batchRepository, never()).save(any(ProductionBatch.class));
    }

    @Test
    @DisplayName("generatePartNumber & generateSerialNumber should produce zero-padded values for 1..10")
    void testSeriesGenerators() {
        assertThat(dataPreparationService.generatePartNumber("PN001", 1)).isEqualTo("PN001");
        assertThat(dataPreparationService.generatePartNumber("PN001", 10)).isEqualTo("PN010");

        assertThat(dataPreparationService.generateSerialNumber("SN001", 1)).isEqualTo("SN001");
        assertThat(dataPreparationService.generateSerialNumber("SN001", 10)).isEqualTo("SN010");
    }

    @Test
    @DisplayName("getAllBatches should return list ordered by creation date")
    void testGetAllBatches() {
        ProductionBatch b1 = ProductionBatch.builder().id(1L).batchId("batch 4").partNoSeries("PN001").partNoCount(10).serialNoSeries("SN001").serialNoCount(10).totalItems(10).build();
        ProductionBatch b2 = ProductionBatch.builder().id(2L).batchId("batch 3").partNoSeries("PN001").partNoCount(5).serialNoSeries("SN001").serialNoCount(5).totalItems(5).build();

        when(batchRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(b1, b2));

        List<ProductionBatchResponse> result = dataPreparationService.getAllBatches();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBatchId()).isEqualTo("batch 4");
        assertThat(result.get(1).getBatchId()).isEqualTo("batch 3");
    }
}
