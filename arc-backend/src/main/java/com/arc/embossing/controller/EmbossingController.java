package com.arc.embossing.controller;

import com.arc.embossing.dto.CurrentMachineResponse;
import com.arc.embossing.dto.EmbossingDashboardResponse;
import com.arc.embossing.dto.EmbossingJobResponse;
import com.arc.embossing.dto.SimulationStartResponse;
import com.arc.embossing.service.EmbossingService;
import com.arc.embossing.service.EmbossingSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/embossing")
public class EmbossingController {

    private final EmbossingService embossingService;
    private final EmbossingSimulationService embossingSimulationService;

    public EmbossingController(
            EmbossingService embossingService,
            EmbossingSimulationService embossingSimulationService) {
        this.embossingService = embossingService;
        this.embossingSimulationService = embossingSimulationService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<EmbossingDashboardResponse> getDashboard() {
        return ResponseEntity.ok(embossingService.getDashboard());
    }

    @GetMapping("/current-machine")
    public ResponseEntity<CurrentMachineResponse> getCurrentMachineJob() {
        return ResponseEntity.ok(embossingService.getCurrentMachineJob());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<EmbossingJobResponse>> getPendingJobs() {
        return ResponseEntity.ok(embossingService.getPendingJobs());
    }

    @GetMapping("/completed")
    public ResponseEntity<List<EmbossingJobResponse>> getCompletedJobs() {
        return ResponseEntity.ok(embossingService.getCompletedJobs());
    }

    @PostMapping("/start")
    public ResponseEntity<SimulationStartResponse> startSimulation() {
        return ResponseEntity.ok(embossingSimulationService.startSimulation());
    }
}
