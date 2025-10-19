package com.example.musicBackend.feature.track.dto;

import com.example.musicBackend.feature.track.domain.Track;

public record TrackResponseDto(
        Long id,
        String spotifyTrackId,
        String title,
        String artist,
        String album,
        Integer durationMs,
        Integer popularity,
        String albumCoverUrl,
        String releaseDate
) {
    public static TrackResponseDto from(Track track) {
        return new TrackResponseDto(
                track.getId(),
                track.getSpotifyTrackId(),
                track.getTitle(),
                track.getArtist(),
                track.getAlbum(),
                track.getDurationMs(),
                track.getPopularity(),
                track.getAlbumCoverUrl(),
                track.getReleaseDate()
        );
    }
}
