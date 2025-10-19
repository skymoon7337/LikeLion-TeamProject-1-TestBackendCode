package com.example.musicBackend.feature.user.dto;

import com.example.musicBackend.feature.user.domain.User;

import java.time.LocalDateTime;

public record UserDetailResponseDto(
        Long id,
        String email,
        String nickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserDetailResponseDto from(User user) {
        return new UserDetailResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}