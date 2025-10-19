package com.example.musicBackend.external.spotify.service;

import com.example.musicBackend.feature.track.dto.TrackResponseDto;
import com.example.musicBackend.feature.track.domain.Track;
import com.example.musicBackend.feature.track.dto.TrackSearchResponseDto;

import java.util.List;

public interface SpotifyService {
    List<TrackSearchResponseDto> searchTracks(String query);
    TrackResponseDto getTrack(String spotifyTrackId);
    Track getOrCreateTrackEntity(String spotifyTrackId);
}
