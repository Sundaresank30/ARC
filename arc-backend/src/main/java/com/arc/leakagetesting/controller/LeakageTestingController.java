package com.arc.leakagetesting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/leakage-testing")
public class LeakageTestingController {

    @GetMapping
    public ResponseEntity<Map<String, String>> getLeakageTesting() {
        return ResponseEntity.ok(Map.of("module", "Leakage Testing", "status", "ok"));
    }
}
