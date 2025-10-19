package com.example.musicBackend.feature.playlist.dto;

import com.example.musicBackend.feature.playlist.domain.PlaylistTrack;
import com.example.musicBackend.feature.track.dto.TrackResponseDto;


public record PlaylistTrackResponseDto(
        Long id,
        TrackResponseDto track,
        Integer position
) {
    public static PlaylistTrackResponseDto from(PlaylistTrack playlistTrack) {
        return new PlaylistTrackResponseDto(
                playlistTrack.getId(),
                TrackResponseDto.from(playlistTrack.getTrack()),
                playlistTrack.getPosition()
        );
    }
}