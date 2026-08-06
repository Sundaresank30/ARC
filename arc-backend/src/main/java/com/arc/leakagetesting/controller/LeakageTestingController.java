package com.arc.leakagetesting.controller;

import com.arc.leakagetesting.dto.LeakageMachineDto;
import com.arc.leakagetesting.dto.LeakageTestItemDto;
import com.arc.leakagetesting.dto.LeakageTestingResponseDto;
import com.arc.leakagetesting.dto.UpdateLeakageActionRequest;
import com.arc.leakagetesting.service.LeakageTestingMachineService;
import com.arc.leakagetesting.service.LeakageTestingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leakage-testing")
public class LeakageTestingController {

    private final LeakageTestingService leakageTestingService;
    private final LeakageTestingMachineService machineService;

    public LeakageTestingController(
            LeakageTestingService leakageTestingService,
            LeakageTestingMachineService machineService) {
        this.leakageTestingService = leakageTestingService;
        this.machineService = machineService;
    }

    @GetMapping
    public ResponseEntity<LeakageTestingResponseDto> getLeakageTestingDashboard() {
        return ResponseEntity.ok(leakageTestingService.getDashboardData());
    }

    @GetMapping("/machine/state")
    public ResponseEntity<LeakageMachineDto.MachineState> getMachineState() {
        return ResponseEntity.ok(machineService.getMachineState());
    }

    @PostMapping("/machine/start")
    public ResponseEntity<LeakageMachineDto.MachineState> startMachine() {
        return ResponseEntity.ok(machineService.startTesting());
    }

    @PostMapping("/machine/pause")
    public ResponseEntity<LeakageMachineDto.MachineState> pauseMachine() {
        return ResponseEntity.ok(machineService.pauseTesting());
    }

    @PostMapping("/machine/reset")
    public ResponseEntity<LeakageMachineDto.MachineState> resetMachine() {
        return ResponseEntity.ok(machineService.resetTesting());
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
