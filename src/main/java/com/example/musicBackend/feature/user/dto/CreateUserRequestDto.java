package com.example.musicBackend.feature.user.dto;

public record CreateUserRequestDto(String email, String password, String nickname) {
}