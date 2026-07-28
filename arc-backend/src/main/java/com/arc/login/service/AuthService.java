package com.arc.login.service;

import com.arc.login.dto.AuthRequest;
import com.arc.login.dto.AuthResponse;
import com.arc.login.dto.RegisterRequest;
import com.arc.login.dto.RoleLoginRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse loginWithRole(RoleLoginRequest request);
}
