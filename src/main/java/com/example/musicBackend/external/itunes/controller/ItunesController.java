package com.example.musicBackend.external.itunes.controller;

import com.example.musicBackend.external.itunes.service.ItunesService;
import com.example.musicBackend.feature.track.dto.TrackSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/itunes")
@RequiredArgsConstructor
public class ItunesController {

    private final ItunesService itunesService;

    @GetMapping("/search")
    public ResponseEntity<List<TrackSearchResponseDto>> searchTracks(@RequestParam("query") String query) {
        List<TrackSearchResponseDto> tracks = itunesService.searchTracks(query);
        return ResponseEntity.ok(tracks);
    }
}
