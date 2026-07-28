package com.arc.login.dto;

import com.arc.login.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleLoginRequest {
    @NotNull(message = "Role cannot be null")
    private Role role;
}
