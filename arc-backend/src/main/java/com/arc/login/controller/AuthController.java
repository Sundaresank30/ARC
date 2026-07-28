package com.arc.login.controller;

import com.arc.login.dto.AuthRequest;
import com.arc.login.dto.AuthResponse;
import com.arc.login.dto.RegisterRequest;
import com.arc.login.dto.RoleLoginRequest;
import com.arc.login.entity.Role;
import com.arc.login.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/role-login")
    public ResponseEntity<AuthResponse> roleLogin(@Valid @RequestBody RoleLoginRequest request) {
        // Map selected role to default credentials for standalone role portal
        String username = (request.getRole() == Role.MANAGER) ? "arc_manager" : "arc_operator";
        String password = (request.getRole() == Role.MANAGER) ? "Manager@123" : "Operator@123";

        AuthRequest authRequest = new AuthRequest(username, password);
        AuthResponse response = authService.login(authRequest);
        response.setRole(request.getRole());
        return ResponseEntity.ok(response);
    }
}
