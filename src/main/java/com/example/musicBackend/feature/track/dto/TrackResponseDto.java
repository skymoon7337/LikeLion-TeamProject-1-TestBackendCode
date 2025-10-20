package com.example.musicBackend.feature.track.dto;

import com.example.musicBackend.feature.track.domain.Track;

public record TrackResponseDto(
        Long id,
        Long trackId,
        String title,
        String artist,
        String album,
        Integer durationMs,
        String albumCoverUrl,
        String releaseDate,
        String previewUrl,
        String primaryGenreName
) {
    public static TrackResponseDto from(Track track) {
        return new TrackResponseDto(
                track.getId(),
                track.getTrackId(),
                track.getTitle(),
                track.getArtist(),
                track.getAlbum(),
                track.getDurationMs(),
                track.getAlbumCoverUrl(),
                track.getReleaseDate(),
                track.getPreviewUrl(),
                track.getPrimaryGenreName()
        );
    }
}
