package com.arc.security;

import com.arc.auth.enums.AppRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public AppRole getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        return AppRole.fromString(authentication.getPrincipal().toString())
                .orElseThrow(() -> new IllegalStateException("Invalid authenticated role"));
    }
}
