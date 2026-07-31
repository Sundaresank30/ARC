package com.arc.auth.service;

import com.arc.auth.enums.AppRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleModuleServiceTest {

    private final RoleModuleService roleModuleService = new RoleModuleService();

    @Test
    void getModulesForRole_manager_returnsDashboardDataPreparationSettings() {
        List<String> modules = roleModuleService.getModulesForRole(AppRole.MANAGER);

        assertThat(modules).containsExactly(
                "Dashboard",
                "Data Preparation",
                "Settings"
        );
    }

    @Test
    void getModulesForRole_operator_returnsEmbossingLeakageMachineSettings() {
        List<String> modules = roleModuleService.getModulesForRole(AppRole.OPERATOR);

        assertThat(modules).containsExactly(
                "Data Embossing",
                "Leakage Testing",
                "Machine",
                "Settings"
        );
    }

    @Test
    void getModulesForRole_manager_doesNotIncludeOperatorModules() {
        List<String> modules = roleModuleService.getModulesForRole(AppRole.MANAGER);

        assertThat(modules).doesNotContain(
                "Data Embossing",
                "Leakage Testing",
                "Machine"
        );
    }

    @Test
    void getModulesForRole_operator_doesNotIncludeManagerModules() {
        List<String> modules = roleModuleService.getModulesForRole(AppRole.OPERATOR);

        assertThat(modules).doesNotContain(
                "Dashboard",
                "Data Preparation"
        );
    }
}
