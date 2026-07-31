package com.arc.machine.controller;

import com.arc.machine.dto.MachineRecordDto;
import com.arc.machine.dto.UpdateStatusRequest;
import com.arc.machine.service.MachineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machine")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping("/records")
    public ResponseEntity<List<MachineRecordDto>> getRecords() {
        return ResponseEntity.ok(machineService.getAllRecords());
    }

    @GetMapping
    public ResponseEntity<List<MachineRecordDto>> getMachineDefault() {
        return ResponseEntity.ok(machineService.getAllRecords());
    }

    @PutMapping("/records/{id}/status")
    public ResponseEntity<MachineRecordDto> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        MachineRecordDto updated = machineService.updateRecordStatus(id, request.getStatus());
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/records/reset")
    public ResponseEntity<List<MachineRecordDto>> resetRecords() {
        return ResponseEntity.ok(machineService.resetRecords());
    }
}
