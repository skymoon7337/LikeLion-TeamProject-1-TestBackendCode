package com.example.musicBackend.feature.playlist.dto;

import com.example.musicBackend.feature.playlist.domain.Playlist;
import com.example.musicBackend.feature.playlist.domain.Visibility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PlaylistResponseDto(
        Long id,
        String title,
        String description,
        String coverImageUrl,
        Visibility visibility,
        Long userId,
        String userNickname,
        List<PlaylistTrackResponseDto> tracks,
        List<Long> sharedUserIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlaylistResponseDto from(Playlist playlist) {
        return new PlaylistResponseDto(
                playlist.getId(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getCoverImageUrl(),
                playlist.getVisibility(),
                playlist.getUser().getId(),
                playlist.getUser().getNickname(),
                playlist.getPlaylistTracks().stream()
                        .sorted((a, b) -> a.getPosition().compareTo(b.getPosition()))
                        .map(PlaylistTrackResponseDto::from)
                        .collect(Collectors.toList()),
                playlist.getPlaylistVisibilities().stream()
                        .map(pv -> pv.getUser().getId())
                        .collect(Collectors.toList()),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }
}
