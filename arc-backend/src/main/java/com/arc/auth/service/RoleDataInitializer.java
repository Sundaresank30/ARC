package com.arc.auth.service;

import com.arc.auth.entity.Role;
import com.arc.auth.enums.AppRole;
import com.arc.auth.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
@Slf4j
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRole(AppRole.MANAGER, "Production manager with dashboard and data preparation access");
        seedRole(AppRole.OPERATOR, "Shop-floor operator with embossing and testing access");
    }

    private void seedRole(AppRole appRole, String description) {
        if (roleRepository.existsByRoleNameIgnoreCase(appRole.name())) {
            return;
        }

        Role role = Role.builder()
                .roleName(appRole.name())
                .description(description)
                .build();

        roleRepository.save(role);
        log.info("Seeded role: {}", appRole.name());
    }
}
