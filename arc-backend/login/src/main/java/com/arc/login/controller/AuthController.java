package com.arc.login.controller;

import com.arc.login.dto.AuthResponse;
import com.arc.login.dto.RoleLoginRequest;
import com.arc.login.entity.Role;
import com.arc.login.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    // ─────────────────────────────────────────────────────────────
    // NEW: Frontend only sends the ROLE — backend assigns default
    //      hardcoded credentials automatically and returns JWT.
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/role-login")
    public ResponseEntity<AuthResponse> roleLogin(@Valid @RequestBody RoleLoginRequest request) {

        // Map the selected role to the hardcoded default credentials
        String username;
        String password;

        if (request.getRole() == Role.MANAGER) {
            username = "arc_manager";
            password = "Manager@123";
        } else {
            username = "arc_operator";
            password = "Operator@123";
        }

        // Authenticate with the default credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // Load user and generate JWT
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(new AuthResponse(jwt, request.getRole().name(), username));
    }
}
