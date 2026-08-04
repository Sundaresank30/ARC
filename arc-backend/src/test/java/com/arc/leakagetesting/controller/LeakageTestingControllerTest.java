package com.arc.leakagetesting.controller;

import com.arc.leakagetesting.dto.LeakageTestItemDto;
import com.arc.leakagetesting.dto.LeakageTestingResponseDto;
import com.arc.leakagetesting.dto.UpdateLeakageActionRequest;
import com.arc.leakagetesting.service.LeakageTestingService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LeakageTestingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LeakageTestingService leakageTestingService;

    @InjectMocks
    private LeakageTestingController leakageTestingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(leakageTestingController).build();
    }

    @Test
    @DisplayName("GET /api/leakage-testing should return dashboard with 0 failures when all items are completed")
    void testGetLeakageTestingDashboard_CleanState() throws Exception {
        LeakageTestingResponseDto mockDashboard = LeakageTestingResponseDto.builder()
                .activeBatch("Batch_1")
                .failedCount(0)
                .batchProgressPercent(100)
                .completedCount(100)
                .totalParts(100)
                .dateDisplay("20 July, 2026")
                .failures(Collections.emptyList())
                .build();

        when(leakageTestingService.getDashboardData()).thenReturn(mockDashboard);

        mockMvc.perform(get("/api/leakage-testing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeBatch").value("Batch_1"))
                .andExpect(jsonPath("$.failedCount").value(0))
                .andExpect(jsonPath("$.batchProgressPercent").value(100))
                .andExpect(jsonPath("$.failures").isEmpty());
    }

    @Test
    @DisplayName("PATCH /api/leakage-testing/jobs/{id}/action should update operator action")
    void testUpdateJobAction() throws Exception {
        UpdateLeakageActionRequest request = new UpdateLeakageActionRequest("Scrap");

        LeakageTestItemDto updatedItem = LeakageTestItemDto.builder()
                .id(1L)
                .partNo("Pn00111c")
                .serialNo("P0011156")
                .status("Failed")
                .testValue(0.42)
                .direction("down")
                .timestamp("17:57, 20 Jul")
                .attempt("2/2")
                .action("Scrap")
                .build();

        when(leakageTestingService.updateJobAction(eq(1L), eq("Scrap"))).thenReturn(updatedItem);

        mockMvc.perform(patch("/api/leakage-testing/jobs/1/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.action").value("Scrap"));
    }

    @Test
    @DisplayName("POST /api/leakage-testing/jobs/{id}/fail should mark job as failed")
    void testMarkJobAsFailed() throws Exception {
        LeakageTestItemDto failedItem = LeakageTestItemDto.builder()
                .id(1L)
                .partNo("Pn00111c")
                .serialNo("P0011156")
                .status("Failed")
                .testValue(0.42)
                .direction("down")
                .timestamp("17:57, 20 Jul")
                .attempt("1/2")
                .action("Pending")
                .build();

        when(leakageTestingService.markJobAsFailed(eq(1L), any(), any(), any(), any()))
                .thenReturn(failedItem);

        mockMvc.perform(post("/api/leakage-testing/jobs/1/fail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Failed"))
                .andExpect(jsonPath("$.testValue").value(0.42));
    }
}
