package com.arc.auth.service;

import com.arc.auth.dto.LoginRequest;
import com.arc.auth.dto.LoginResponse;
import com.arc.auth.enums.AppRole;
import com.arc.auth.repository.RoleRepository;
import com.arc.exception.InvalidRoleException;
import com.arc.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleModuleService roleModuleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_withValidManagerRole_returnsTokenAndModules() {
        LoginRequest request = new LoginRequest("MANAGER");
        List<String> modules = List.of("Dashboard", "Data Preparation", "Settings");

        when(roleRepository.existsByRoleNameIgnoreCase("MANAGER")).thenReturn(true);
        when(jwtService.generateToken(AppRole.MANAGER)).thenReturn("jwt-token");
        when(roleModuleService.getModulesForRole(AppRole.MANAGER)).thenReturn(modules);

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("MANAGER");
        assertThat(response.getModules()).containsExactlyElementsOf(modules);
        verify(jwtService).generateToken(AppRole.MANAGER);
    }

    @Test
    void login_withValidOperatorRole_returnsTokenAndModules() {
        LoginRequest request = new LoginRequest("operator");
        List<String> modules = List.of("Data Embossing", "Leakage Testing", "Machine", "Settings");

        when(roleRepository.existsByRoleNameIgnoreCase("OPERATOR")).thenReturn(true);
        when(jwtService.generateToken(AppRole.OPERATOR)).thenReturn("operator-token");
        when(roleModuleService.getModulesForRole(AppRole.OPERATOR)).thenReturn(modules);

        LoginResponse response = authService.login(request);

        assertThat(response.getRole()).isEqualTo("OPERATOR");
        assertThat(response.getModules()).hasSize(4);
    }

    @Test
    void login_withInvalidRole_throwsInvalidRoleException() {
        LoginRequest request = new LoginRequest("ADMIN");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidRoleException.class)
                .hasMessage("Invalid role selected");
    }

    @Test
    void login_withUnknownRoleInDatabase_throwsInvalidRoleException() {
        LoginRequest request = new LoginRequest("MANAGER");

        when(roleRepository.existsByRoleNameIgnoreCase("MANAGER")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidRoleException.class)
                .hasMessage("Invalid role selected");
    }
}
