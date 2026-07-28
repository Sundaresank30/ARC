package com.arc.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OperatorController {

    @GetMapping("/data-embossing")
    public ResponseEntity<String> dataEmbossing() {
        return ResponseEntity.ok("Data Embossing Module - Operator Access Only");
    }

    @GetMapping("/leakage-testing")
    public ResponseEntity<String> leakageTesting() {
        return ResponseEntity.ok("Leakage Testing Module - Operator Access Only");
    }
}
