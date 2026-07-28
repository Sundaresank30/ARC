package com.arc.login.service.impl;

import com.arc.login.dto.AuthRequest;
import com.arc.login.dto.AuthResponse;
import com.arc.login.dto.RegisterRequest;
import com.arc.login.dto.RoleLoginRequest;
import com.arc.login.entity.Role;
import com.arc.login.entity.User;
import com.arc.login.repository.UserRepository;
import com.arc.login.service.AuthService;
import com.arc.login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse loginWithRole(RoleLoginRequest request) {
        String username = (request.getRole() == Role.MANAGER) ? "arc_manager" : "arc_operator";
        String password = (request.getRole() == Role.MANAGER) ? "Manager@123" : "Operator@123";

        return login(new AuthRequest(username, password));
    }
}
