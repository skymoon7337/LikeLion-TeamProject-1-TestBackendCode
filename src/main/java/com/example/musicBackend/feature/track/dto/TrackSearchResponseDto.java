package com.example.musicBackend.feature.track.dto;

public record TrackSearchResponseDto(
        String spotifyTrackId,
        String title,
        String artist,
        String album,
        Integer durationMs,
        Integer popularity,
        String albumCoverUrl,
        String releaseDate
) {
}
