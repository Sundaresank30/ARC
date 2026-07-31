package com.arc.datapreparation.controller;

import com.arc.datapreparation.dto.CreateProductionBatchRequest;
import com.arc.datapreparation.dto.ProductionBatchItemDto;
import com.arc.datapreparation.dto.ProductionBatchResponse;
import com.arc.datapreparation.service.DataPreparationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DataPreparationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DataPreparationService dataPreparationService;

    @InjectMocks
    private DataPreparationController dataPreparationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dataPreparationController).build();
    }

    @Test
    @DisplayName("POST /api/data-preparation/batches should return 201 CREATED with batch details")
    void testCreateProductionBatch() throws Exception {
        CreateProductionBatchRequest request = CreateProductionBatchRequest.builder()
                .batchId("batch 4")
                .partNoSeries("PN001")
                .partNoCount(10)
                .serialNoSeries("SN001")
                .serialNoCount(10)
                .build();

        ProductionBatchItemDto item1 = ProductionBatchItemDto.builder()
                .id(1L)
                .itemIndex(1)
                .partNumber("PN001")
                .serialNumber("SN001")
                .status("PREPARED")
                .build();

        ProductionBatchResponse mockResponse = ProductionBatchResponse.builder()
                .id(1L)
                .batchId("batch 4")
                .partNoSeries("PN001")
                .partNoCount(10)
                .serialNoSeries("SN001")
                .serialNoCount(10)
                .totalItems(10)
                .createdAt(LocalDateTime.now())
                .items(Collections.singletonList(item1))
                .build();

        when(dataPreparationService.createProductionBatch(any(CreateProductionBatchRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/data-preparation/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batchId").value("batch 4"))
                .andExpect(jsonPath("$.totalItems").value(10))
                .andExpect(jsonPath("$.items[0].partNumber").value("PN001"))
                .andExpect(jsonPath("$.items[0].serialNumber").value("SN001"));
    }

    @Test
    @DisplayName("GET /api/data-preparation/batches should return list of production batches")
    void testGetAllBatches() throws Exception {
        ProductionBatchResponse b1 = ProductionBatchResponse.builder()
                .id(1L)
                .batchId("batch 4")
                .totalItems(10)
                .build();

        when(dataPreparationService.getAllBatches()).thenReturn(Collections.singletonList(b1));

        mockMvc.perform(get("/api/data-preparation/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batchId").value("batch 4"))
                .andExpect(jsonPath("$[0].totalItems").value(10));
    }
}
