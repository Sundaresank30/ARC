package com.arc.login.service.impl;

import com.arc.login.dto.UserResponse;
import com.arc.login.entity.User;
import com.arc.login.exception.ResourceNotFoundException;
import com.arc.login.repository.UserRepository;
import com.arc.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = findEntityByUsername(username);
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public User findEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
}
