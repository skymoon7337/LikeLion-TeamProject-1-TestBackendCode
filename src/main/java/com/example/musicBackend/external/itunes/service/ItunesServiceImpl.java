package com.example.musicBackend.external.itunes.service;

import com.example.musicBackend.external.itunes.dto.ItunesResponse;
import com.example.musicBackend.external.itunes.dto.ItunesTrackDto;
import com.example.musicBackend.feature.track.domain.Track;
import com.example.musicBackend.feature.track.dto.TrackSearchResponseDto;
import com.example.musicBackend.feature.track.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItunesServiceImpl implements ItunesService {

    private final WebClient webClient;
    private final TrackRepository trackRepository;
    private static final String ITUNES_SEARCH_URL = "https://itunes.apple.com/search";
    private static final String ITUNES_LOOKUP_URL = "https://itunes.apple.com/lookup";

    @Override
    public List<TrackSearchResponseDto> searchTracks(String query) {
        log.info("🔍 iTunes 검색 시작 (WebClient) - query: {}", query);

        String uriString = UriComponentsBuilder.fromHttpUrl(ITUNES_SEARCH_URL)
                .queryParam("term", query)
                .queryParam("media", "music")
                .queryParam("entity", "song")
                .queryParam("limit", 20)
                .toUriString();

        try {
            ItunesResponse response = webClient.get()
                    .uri(uriString)
                    .retrieve()
                    .bodyToMono(ItunesResponse.class)
                    .block();

            if (response == null || response.getResults() == null) {
                log.warn("iTunes API로부터 응답이 없거나 결과가 비어있습니다.");
                return Collections.emptyList();
            }

            log.info("✅ iTunes 검색 완료 - 결과 수: {}", response.getResultCount());

            return response.getResults().stream()
                    .map(this::mapToTrackSearchResponseDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ iTunes 검색 실패 (WebClient)", e);
            throw new RuntimeException("Failed to search tracks from iTunes", e);
        }
    }

    @Override
    @Transactional
    public Track getOrCreateTrackEntity(Long trackId) {
        Optional<Track> existingTrack = trackRepository.findByTrackId(trackId);
        if (existingTrack.isPresent()) {
            return existingTrack.get();
        }

        ItunesTrackDto itunesTrack = fetchTrackFromApi(trackId);
        Track track = mapToEntity(itunesTrack);
        return trackRepository.save(track);
    }

    private ItunesTrackDto fetchTrackFromApi(Long trackId) {
        log.info("🎵 iTunes API로 곡 조회 - trackId: {}", trackId);
        String uriString = UriComponentsBuilder.fromHttpUrl(ITUNES_LOOKUP_URL)
                .queryParam("id", trackId)
                .toUriString();

        ItunesResponse response = webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(ItunesResponse.class)
                .block();

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            log.error("❌ iTunes 곡 조회 실패 - trackId: {}", trackId);
            throw new RuntimeException("Failed to get track from iTunes API");
        }
        return response.getResults().get(0);
    }

    private Track mapToEntity(ItunesTrackDto itunesTrack) {
        return Track.builder()
                .trackId(itunesTrack.getTrackId())
                .title(itunesTrack.getTrackName())
                .artist(itunesTrack.getArtistName())
                .album(itunesTrack.getCollectionName())
                .durationMs(itunesTrack.getTrackTimeMillis() != null ? itunesTrack.getTrackTimeMillis().intValue() : null)
                .albumCoverUrl(itunesTrack.getArtworkUrl100())
                .releaseDate(itunesTrack.getReleaseDate())
                .previewUrl(itunesTrack.getPreviewUrl())
                .primaryGenreName(itunesTrack.getPrimaryGenreName())
                .build();
    }

    private TrackSearchResponseDto mapToTrackSearchResponseDto(ItunesTrackDto itunesTrack) {
        return new TrackSearchResponseDto(
                String.valueOf(itunesTrack.getTrackId()),
                itunesTrack.getTrackName(),
                itunesTrack.getArtistName(),
                itunesTrack.getCollectionName(),
                itunesTrack.getTrackTimeMillis() != null ? itunesTrack.getTrackTimeMillis().intValue() : 0,
                itunesTrack.getArtworkUrl100(),
                itunesTrack.getReleaseDate(),
                itunesTrack.getPreviewUrl(),
                itunesTrack.getPrimaryGenreName()
        );
    }
}
