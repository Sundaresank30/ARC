package com.arc.login.repository;

import com.arc.login.entity.Role;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class RoleRepository {

    public List<Role> findAll() {
        return Arrays.asList(Role.values());
    }

    public boolean exists(String roleName) {
        return Arrays.stream(Role.values())
                .anyMatch(r -> r.name().equalsIgnoreCase(roleName));
    }
}
