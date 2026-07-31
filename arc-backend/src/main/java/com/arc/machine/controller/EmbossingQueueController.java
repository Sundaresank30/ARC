package com.arc.machine.controller;

import com.arc.machine.dto.EmbossingQueueDto;
import com.arc.machine.service.EmbossingQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST API for the Machine Module Queue Engine.
 *
 * All queue state mutations happen here; the frontend drives the loop timing.
 *
 * Endpoints:
 *   GET  /api/machine/queue/buffer        — top 5 non-COMPLETED records (initial load)
 *   PUT  /api/machine/queue/{id}/in-progress — mark item IN_PROGRESS
 *   PUT  /api/machine/queue/{id}/complete    — mark item COMPLETED + timestamps
 *   GET  /api/machine/queue/next-waiting     — next WAITING record (buffer refill)
 *   POST /api/machine/queue/reset            — reset all rows to WAITING
 */
@RestController
@RequestMapping("/api/machine/queue")
public class EmbossingQueueController {

    private final EmbossingQueueService queueService;

    public EmbossingQueueController(EmbossingQueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/buffer")
    public ResponseEntity<List<EmbossingQueueDto>> getBuffer() {
        return ResponseEntity.ok(queueService.getQueueBuffer());
    }

    @PutMapping("/{id}/in-progress")
    public ResponseEntity<EmbossingQueueDto> markInProgress(@PathVariable Long id) {
        return ResponseEntity.ok(queueService.markInProgress(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<EmbossingQueueDto> markCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(queueService.markCompleted(id));
    }

    @GetMapping("/next-waiting")
    public ResponseEntity<EmbossingQueueDto> nextWaiting() {
        Optional<EmbossingQueueDto> next = queueService.fetchNextWaiting();
        return next.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/claim-next")
    public ResponseEntity<EmbossingQueueDto> claimNextWaiting() {
        Optional<EmbossingQueueDto> next = queueService.claimNextWaiting();
        return next.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/reset")
    public ResponseEntity<List<EmbossingQueueDto>> reset() {
        return ResponseEntity.ok(queueService.resetAll());
    }
}
