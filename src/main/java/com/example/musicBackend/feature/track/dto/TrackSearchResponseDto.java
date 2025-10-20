package com.example.musicBackend.feature.track.dto;

public record TrackSearchResponseDto(
        String trackId,
        String title,
        String artist,
        String album,
        Integer durationMs,
        String albumCoverUrl,
        String releaseDate
) {
}
