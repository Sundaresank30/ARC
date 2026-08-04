package com.arc.leakagetesting.controller;

import com.arc.leakagetesting.dto.LeakageTestItemDto;
import com.arc.leakagetesting.dto.LeakageTestingResponseDto;
import com.arc.leakagetesting.dto.UpdateLeakageActionRequest;
import com.arc.leakagetesting.service.LeakageTestingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leakage-testing")
public class LeakageTestingController {

    private final LeakageTestingService leakageTestingService;

    public LeakageTestingController(LeakageTestingService leakageTestingService) {
        this.leakageTestingService = leakageTestingService;
    }

    @GetMapping
    public ResponseEntity<LeakageTestingResponseDto> getLeakageTestingDashboard() {
        return ResponseEntity.ok(leakageTestingService.getDashboardData());
    }

    @PatchMapping("/jobs/{id}/action")
    public ResponseEntity<LeakageTestItemDto> updateJobAction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeakageActionRequest request) {
        return ResponseEntity.ok(leakageTestingService.updateJobAction(id, request.getAction()));
    }

    @PostMapping("/jobs/{id}/fail")
    public ResponseEntity<LeakageTestItemDto> markJobAsFailed(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "0.42") Double testValue,
            @RequestParam(required = false, defaultValue = "down") String direction,
            @RequestParam(required = false, defaultValue = "1/2") String attempt,
            @RequestParam(required = false, defaultValue = "Pending") String action) {
        return ResponseEntity.ok(leakageTestingService.markJobAsFailed(id, testValue, direction, attempt, action));
    }
}
