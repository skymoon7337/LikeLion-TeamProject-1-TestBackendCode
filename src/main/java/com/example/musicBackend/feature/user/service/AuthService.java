package com.example.musicBackend.feature.user.service;

import com.example.musicBackend.feature.user.dto.LoginRequestDto;
import com.example.musicBackend.feature.user.dto.SignupRequestDto;
import com.example.musicBackend.feature.user.dto.AuthResponseDto;

public interface AuthService {
    AuthResponseDto signup(SignupRequestDto request);
    AuthResponseDto login(LoginRequestDto request);
}