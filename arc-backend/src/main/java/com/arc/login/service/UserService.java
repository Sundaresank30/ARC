package com.arc.login.service;

import com.arc.login.dto.UserResponse;
import com.arc.login.entity.User;

import java.util.List;

public interface UserService {
    UserResponse getUserByUsername(String username);
    List<UserResponse> getAllUsers();
    User findEntityByUsername(String username);
}
