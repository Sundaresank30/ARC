package com.arc.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ManagerController {

    @GetMapping("/data-preparation")
    public ResponseEntity<String> dataPreparation() {
        return ResponseEntity.ok("Data Preparation Module - Manager Access Only");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("Dashboard Module - Manager Access Only");
    }

    @GetMapping("/settings")
    public ResponseEntity<String> settings() {
        return ResponseEntity.ok("Settings Module - Authorized Access (Manager & Operator)");
    }
}
