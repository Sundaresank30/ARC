package com.arc.dashboard.controller;

import com.arc.dashboard.dto.CarryForwardDTO;
import com.arc.dashboard.dto.DashboardResponseDTO;
import com.arc.dashboard.dto.LeakageFailureDTO;
import com.arc.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/carry-forward")
    public ResponseEntity<List<CarryForwardDTO>> getCarryForward() {
        return ResponseEntity.ok(dashboardService.getCarryForwardItems());
    }

    @GetMapping("/leakage-failures")
    public ResponseEntity<List<LeakageFailureDTO>> getLeakageFailures() {
        return ResponseEntity.ok(dashboardService.getLeakageFailures());
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
