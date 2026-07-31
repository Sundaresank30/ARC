package com.arc.auth.service;

import com.arc.auth.dto.LoginRequest;
import com.arc.auth.dto.LoginResponse;
import com.arc.auth.dto.MeResponse;
import com.arc.auth.enums.AppRole;
import com.arc.auth.repository.RoleRepository;
import com.arc.exception.InvalidRoleException;
import com.arc.security.jwt.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_ROLE_MESSAGE = "Invalid role selected";

    private final RoleRepository roleRepository;
    private final RoleModuleService roleModuleService;
    private final JwtService jwtService;

    public AuthService(
            RoleRepository roleRepository,
            RoleModuleService roleModuleService,
            JwtService jwtService) {
        this.roleRepository = roleRepository;
        this.roleModuleService = roleModuleService;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        AppRole appRole = resolveRole(request.getRole());
        String token = jwtService.generateToken(appRole);

        return LoginResponse.builder()
                .token(token)
                .role(appRole.name())
                .modules(roleModuleService.getModulesForRole(appRole))
                .build();
    }

    public MeResponse getCurrentUser(AppRole appRole) {
        return MeResponse.builder()
                .role(appRole.name())
                .modules(roleModuleService.getModulesForRole(appRole))
                .build();
    }

    private AppRole resolveRole(String roleValue) {
        AppRole appRole = AppRole.fromString(roleValue)
                .orElseThrow(() -> new InvalidRoleException(INVALID_ROLE_MESSAGE));

        if (!roleRepository.existsByRoleNameIgnoreCase(appRole.name())) {
            throw new InvalidRoleException(INVALID_ROLE_MESSAGE);
        }

        return appRole;
    }
}
