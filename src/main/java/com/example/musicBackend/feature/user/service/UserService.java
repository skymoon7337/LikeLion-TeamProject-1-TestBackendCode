package com.example.musicBackend.feature.user.service;

import com.example.musicBackend.feature.user.dto.CreateUserRequestDto;
import com.example.musicBackend.feature.user.dto.UserDetailResponseDto;

import java.util.List;

public interface UserService {
    UserDetailResponseDto createUser(CreateUserRequestDto request);
    List<UserDetailResponseDto> getAllUsers();
    UserDetailResponseDto getUserById(Long userId);
    UserDetailResponseDto updateUser(Long userId, CreateUserRequestDto request);
    void deleteUser(Long userId);
}