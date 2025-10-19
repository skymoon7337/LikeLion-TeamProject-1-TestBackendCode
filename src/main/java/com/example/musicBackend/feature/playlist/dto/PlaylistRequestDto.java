package com.example.musicBackend.feature.playlist.dto;

import com.example.musicBackend.feature.playlist.domain.Visibility;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PlaylistRequestDto(
        @NotBlank
        String title,
        String description,
        String coverImageUrl,
        Visibility visibility,
        List<Long> sharedUserIds
) {}
