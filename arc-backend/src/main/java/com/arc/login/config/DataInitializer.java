package com.arc.login.config;

import com.arc.login.entity.Role;
import com.arc.login.entity.User;
import com.arc.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername("arc_manager").isEmpty()) {
            User manager = User.builder()
                    .username("arc_manager")
                    .password(passwordEncoder.encode("Manager@123"))
                    .role(Role.MANAGER)
                    .build();
            userRepository.save(manager);
            log.info("✅ Default MANAGER user created: arc_manager / Manager@123");
        }

        if (userRepository.findByUsername("arc_operator").isEmpty()) {
            User operator = User.builder()
                    .username("arc_operator")
                    .password(passwordEncoder.encode("Operator@123"))
                    .role(Role.OPERATOR)
                    .build();
            userRepository.save(operator);
            log.info("✅ Default OPERATOR user created: arc_operator / Operator@123");
        }
    }
}
