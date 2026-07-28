package com.arc.login.dto;

import com.arc.login.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Role role;
    private String username;

    public AuthResponse(String token, String roleStr, String username) {
        this.token = token;
        this.username = username;
        if (roleStr != null) {
            try {
                this.role = Role.valueOf(roleStr.toUpperCase());
            } catch (Exception e) {
                this.role = null;
            }
        }
    }
}
