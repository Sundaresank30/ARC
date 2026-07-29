package com.arc.machine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/machine")
public class MachineController {

    @GetMapping
    public ResponseEntity<Map<String, String>> getMachine() {
        return ResponseEntity.ok(Map.of("module", "Machine", "status", "ok"));
    }
}
