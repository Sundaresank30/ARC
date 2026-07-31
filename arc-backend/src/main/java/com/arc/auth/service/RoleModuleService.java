package com.arc.auth.service;

import com.arc.auth.enums.AppRole;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleModuleService {

    private static final Map<AppRole, List<String>> ROLE_MODULES = new EnumMap<>(AppRole.class);

    static {
        ROLE_MODULES.put(AppRole.MANAGER, List.of(
                "Dashboard",
                "Data Preparation",
                "Settings"
        ));

        ROLE_MODULES.put(AppRole.OPERATOR, List.of(
                "Data Embossing",
                "Leakage Testing",
                "Machine",
                "Settings"
        ));
    }

    public List<String> getModulesForRole(AppRole role) {
        return ROLE_MODULES.getOrDefault(role, List.of());
    }
}
