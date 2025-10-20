package com.example.musicBackend.external.itunes.service;

import com.example.musicBackend.feature.track.domain.Track;
import com.example.musicBackend.feature.track.dto.TrackSearchResponseDto;

import java.util.List;

public interface ItunesService {
    List<TrackSearchResponseDto> searchTracks(String query);

    Track getOrCreateTrackEntity(Long trackId);
}
