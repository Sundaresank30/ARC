package com.arc.datapreparation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/data-preparation")
public class DataPreparationController {

    @GetMapping
    public ResponseEntity<Map<String, String>> getDataPreparation() {
        return ResponseEntity.ok(Map.of("module", "Data Preparation", "status", "ok"));
    }
}
