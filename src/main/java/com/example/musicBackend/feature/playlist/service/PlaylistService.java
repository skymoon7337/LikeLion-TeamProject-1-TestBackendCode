package com.example.musicBackend.feature.playlist.service;

import com.example.musicBackend.feature.playlist.dto.PlaylistResponseDto;
import com.example.musicBackend.feature.playlist.dto.PlaylistRequestDto;

import java.util.List;

public interface PlaylistService {
    PlaylistResponseDto createPlaylist(Long userId, PlaylistRequestDto request);
    List<PlaylistResponseDto> getUserPlaylists(Long userId);
    List<PlaylistResponseDto> getPublicPlaylists();
    PlaylistResponseDto getPlaylist(Long playlistId);
    PlaylistResponseDto updatePlaylist(Long playlistId, Long userId, PlaylistRequestDto request);
    void deletePlaylist(Long playlistId, Long userId);
    PlaylistResponseDto addTrackToPlaylist(Long playlistId, Long userId, Long trackId);
    void removeTrackFromPlaylist(Long playlistId, Long userId, Long trackId);
    PlaylistResponseDto updateTrackPosition(Long playlistId, Long userId, Long trackId, Integer newPosition);
}