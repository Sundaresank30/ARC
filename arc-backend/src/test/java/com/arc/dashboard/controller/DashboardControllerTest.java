package com.arc.dashboard.controller;

import com.arc.dashboard.dto.CarryForwardDTO;
import com.arc.dashboard.dto.DashboardResponseDTO;
import com.arc.dashboard.dto.LeakageFailureDTO;
import com.arc.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    @DisplayName("GET /api/dashboard returns 200 OK and DashboardResponseDTO")
    void testGetDashboard() throws Exception {
        CarryForwardDTO cf = CarryForwardDTO.builder()
                .id("1")
                .partNo("PN01")
                .serialNo("P01")
                .status("Pending")
                .build();

        LeakageFailureDTO lf = LeakageFailureDTO.builder()
                .id("2")
                .partNo("PN02")
                .serialNo("P02")
                .status("Failed")
                .testValue(84.0)
                .build();

        DashboardResponseDTO response = DashboardResponseDTO.builder()
                .completedCount(10)
                .failedCount(1)
                .totalBatches(2)
                .carryForwardEmbossing(List.of(cf))
                .leakageTestingFailures(List.of(lf))
                .build();

        when(dashboardService.getDashboardSummary()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedCount").value(10))
                .andExpect(jsonPath("$.failedCount").value(1))
                .andExpect(jsonPath("$.totalBatches").value(2))
                .andExpect(jsonPath("$.carryForwardEmbossing[0].partNo").value("PN01"))
                .andExpect(jsonPath("$.leakageTestingFailures[0].partNo").value("PN02"));
    }

    @Test
    @DisplayName("GET /api/dashboard/carry-forward returns list of CarryForwardDTO")
    void testGetCarryForward() throws Exception {
        CarryForwardDTO cf = CarryForwardDTO.builder()
                .id("1")
                .partNo("PN01")
                .serialNo("P01")
                .status("Pending")
                .build();

        when(dashboardService.getCarryForwardItems()).thenReturn(List.of(cf));

        mockMvc.perform(get("/api/dashboard/carry-forward"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].partNo").value("PN01"));
    }

    @Test
    @DisplayName("GET /api/dashboard/leakage-failures returns list of LeakageFailureDTO")
    void testGetLeakageFailures() throws Exception {
        LeakageFailureDTO lf = LeakageFailureDTO.builder()
                .id("2")
                .partNo("PN02")
                .serialNo("P02")
                .status("Failed")
                .testValue(82.5)
                .build();

        when(dashboardService.getLeakageFailures()).thenReturn(List.of(lf));

        mockMvc.perform(get("/api/dashboard/leakage-failures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].partNo").value("PN02"));
    }

    @Test
    @DisplayName("POST /api/dashboard/carry-forward/{id}/resolve calls service resolveCarryForward")
    void testResolveCarryForward() throws Exception {
        doNothing().when(dashboardService).resolveCarryForward(1L);

        mockMvc.perform(post("/api/dashboard/carry-forward/1/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Embossing carry-forward item resolved"));
    }

    @Test
    @DisplayName("POST /api/dashboard/leakage-failures/{id}/resolve calls service resolveLeakageFailure")
    void testResolveLeakageFailure() throws Exception {
        doNothing().when(dashboardService).resolveLeakageFailure(2L);

        mockMvc.perform(post("/api/dashboard/leakage-failures/2/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leakage failure item resolved"));
    }
}
