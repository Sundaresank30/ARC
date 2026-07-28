package com.arc.dataembossing.controller;

import com.arc.dataembossing.dto.*;
import com.arc.dataembossing.service.EmbossingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-embossing")
@RequiredArgsConstructor
public class EmbossingController {

    private final EmbossingService embossingService;

    // ── Start Embossing API ───────────────────────────────────────────────────
    @PostMapping("/start")
    public ResponseEntity<EmbossingResponse> startEmbossing(@Valid @RequestBody StartEmbossingRequest request) {
        return ResponseEntity.ok(embossingService.startEmbossing(request));
    }

    // ── Receive Machine Production Data ───────────────────────────────────────
    @PostMapping("/receive-data")
    public ResponseEntity<EmbossingResponse> receiveMachineData(@Valid @RequestBody MachineDataRequest request) {
        return ResponseEntity.ok(embossingService.receiveMachineData(request));
    }

    // ── Complete Production Batch ─────────────────────────────────────────────
    @PostMapping("/complete/{batchId}")
    public ResponseEntity<EmbossingResponse> completeBatch(@PathVariable String batchId) {
        return ResponseEntity.ok(embossingService.completeBatch(batchId));
    }

    // ── Retrieve Production Logs ──────────────────────────────────────────────
    @GetMapping("/logs/{batchId}")
    public ResponseEntity<List<ProductionLogResponse>> getProductionLogs(@PathVariable Long batchId) {
        return ResponseEntity.ok(embossingService.getProductionLogs(batchId));
    }

    // ── Global Error Handler ──────────────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
