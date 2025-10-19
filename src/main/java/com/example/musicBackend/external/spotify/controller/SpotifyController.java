package com.example.musicBackend.external.spotify.controller;

import com.example.musicBackend.external.spotify.service.SpotifyService;
import com.example.musicBackend.feature.track.dto.TrackResponseDto;
import com.example.musicBackend.feature.track.dto.TrackSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    /**
     * Spotify에서 곡 검색
     *
     * @param q 검색 키워드
     * @return 검색 결과 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<TrackSearchResponseDto>> searchTracks(@RequestParam String q) {
        return ResponseEntity.ok(spotifyService.searchTracks(q));
    }

    /**
     * Spotify ID로 곡 상세 정보 조회
     *
     * @param spotifyTrackId Spotify 곡 ID
     * @return 곡 상세 정보
     */
    @GetMapping("/tracks/{spotifyTrackId}")
    public ResponseEntity<TrackResponseDto> getTrack(@PathVariable String spotifyTrackId) {
        return ResponseEntity.ok(spotifyService.getTrack(spotifyTrackId));
    }
}
