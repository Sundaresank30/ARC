package com.arc.dashboard.controller;

import com.arc.dashboard.dto.DashboardResponseDTO;
import com.arc.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @PostMapping("/carry-forward/{id}/resolve")
    public ResponseEntity<Map<String, String>> resolveCarryForward(@PathVariable Long id) {
        dashboardService.resolveCarryForward(id);
        return ResponseEntity.ok(Map.of("message", "Embossing carry-forward item resolved"));
    }

    @PostMapping("/leakage-failures/{id}/resolve")
    public ResponseEntity<Map<String, String>> resolveLeakageFailure(@PathVariable Long id) {
        dashboardService.resolveLeakageFailure(id);
        return ResponseEntity.ok(Map.of("message", "Leakage failure item resolved"));
    }
}
