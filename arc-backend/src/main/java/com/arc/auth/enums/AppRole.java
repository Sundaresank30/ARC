package com.arc.auth.enums;

import java.util.Arrays;
import java.util.Optional;

public enum AppRole {
    MANAGER,
    OPERATOR;

    public static Optional<AppRole> fromString(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
