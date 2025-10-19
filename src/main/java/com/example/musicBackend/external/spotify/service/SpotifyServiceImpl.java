package com.example.musicBackend.external.spotify.service;

import com.example.musicBackend.feature.track.dto.TrackResponseDto;
import com.example.musicBackend.feature.track.domain.Track;
import com.example.musicBackend.feature.track.dto.TrackSearchResponseDto;
import com.example.musicBackend.feature.track.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.model_objects.specification.Paging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyServiceImpl implements SpotifyService {

    private final TrackRepository trackRepository;

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;

    private SpotifyApi getSpotifyApi() {
        if (spotifyApi == null) {
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .build();

            try {
                ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
                ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                log.info("✅ Spotify API 인증 완료");
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                log.error("❌ Spotify API 인증 실패", e);
                throw new RuntimeException("Failed to authenticate with Spotify", e);
            }
        }
        return spotifyApi;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackSearchResponseDto> searchTracks(String query) {
        log.info("🔍 Spotify 검색 시작 - query: {}", query);
        try {
            SpotifyApi api = getSpotifyApi();
            SearchTracksRequest searchTracksRequest = api.searchTracks(query)
                    .limit(20)
                    .market(com.neovisionaries.i18n.CountryCode.KR)
                    .build();

            Paging<se.michaelthelin.spotify.model_objects.specification.Track> trackPaging = searchTracksRequest.execute();
            log.info("✅ Spotify 검색 완료 - 결과 수: {}", trackPaging.getItems().length);

            List<TrackSearchResponseDto> tracks = new ArrayList<>();
            for (se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack : trackPaging.getItems()) {
                log.debug("처리 중: {}", spotifyTrack.getName());
                tracks.add(mapToSearchDto(spotifyTrack));
            }

            log.info("✅ 총 {} 곡 처리 완료", tracks.size());
            return tracks;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("❌ Spotify 검색 실패", e);
            throw new RuntimeException("Failed to search tracks", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TrackResponseDto getTrack(String spotifyTrackId) {
        log.info("🎵 곡 조회 - spotifyTrackId: {}", spotifyTrackId);

        Optional<Track> existingTrack = trackRepository.findBySpotifyTrackId(spotifyTrackId);
        if (existingTrack.isPresent()) {
            log.info("✅ DB 캐시에서 곡 조회 성공");
            return TrackResponseDto.from(existingTrack.get());
        }

        se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack = fetchTrackFromApi(spotifyTrackId);
        log.info("✅ Spotify API에서 곡 조회 완료");
        return mapToTrackResponseDto(spotifyTrack);
    }

    @Override
    @Transactional
    public Track getOrCreateTrackEntity(String spotifyTrackId) {
        Optional<Track> existingTrack = trackRepository.findBySpotifyTrackId(spotifyTrackId);
        if (existingTrack.isPresent()) {
            return existingTrack.get();
        }

        se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack = fetchTrackFromApi(spotifyTrackId);
        Track track = mapToEntity(spotifyTrack);
        return trackRepository.save(track);
    }

    private se.michaelthelin.spotify.model_objects.specification.Track fetchTrackFromApi(String spotifyTrackId) {
        try {
            SpotifyApi api = getSpotifyApi();
            return api.getTrack(spotifyTrackId)
                    .market(com.neovisionaries.i18n.CountryCode.KR)
                    .build()
                    .execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("❌ Spotify 곡 조회 실패", e);
            throw new RuntimeException("Failed to get track", e);
        }
    }

    private Track mapToEntity(se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack) {
        String artist = spotifyTrack.getArtists() != null && spotifyTrack.getArtists().length > 0
                ? Arrays.stream(spotifyTrack.getArtists())
                .map(a -> a.getName())
                .collect(Collectors.joining(", "))
                : "Unknown Artist";

        String albumCoverUrl = spotifyTrack.getAlbum() != null
                && spotifyTrack.getAlbum().getImages() != null
                && spotifyTrack.getAlbum().getImages().length > 0
                ? spotifyTrack.getAlbum().getImages()[0].getUrl()
                : null;

        return Track.builder()
                .spotifyTrackId(spotifyTrack.getId())
                .title(spotifyTrack.getName())
                .artist(artist)
                .album(spotifyTrack.getAlbum() != null ? spotifyTrack.getAlbum().getName() : null)
                .durationMs(spotifyTrack.getDurationMs())
                .popularity(spotifyTrack.getPopularity())
                .albumCoverUrl(albumCoverUrl)
                .releaseDate(spotifyTrack.getAlbum() != null ? spotifyTrack.getAlbum().getReleaseDate() : null)
                .build();
    }

    private TrackSearchResponseDto mapToSearchDto(se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack) {
        String artist = spotifyTrack.getArtists() != null && spotifyTrack.getArtists().length > 0
                ? Arrays.stream(spotifyTrack.getArtists())
                .map(a -> a.getName())
                .collect(Collectors.joining(", "))
                : "Unknown Artist";

        String albumCoverUrl = spotifyTrack.getAlbum() != null
                && spotifyTrack.getAlbum().getImages() != null
                && spotifyTrack.getAlbum().getImages().length > 0
                ? spotifyTrack.getAlbum().getImages()[0].getUrl()
                : null;

        return new TrackSearchResponseDto(
                spotifyTrack.getId(),
                spotifyTrack.getName(),
                artist,
                spotifyTrack.getAlbum() != null ? spotifyTrack.getAlbum().getName() : null,
                spotifyTrack.getDurationMs(),
                spotifyTrack.getPopularity(),
                albumCoverUrl,
                spotifyTrack.getAlbum() != null ? spotifyTrack.getAlbum().getReleaseDate() : null
        );
    }

    private TrackResponseDto mapToTrackResponseDto(se.michaelthelin.spotify.model_objects.specification.Track spotifyTrack) {
        TrackSearchResponseDto searchDto = mapToSearchDto(spotifyTrack);
        return new TrackResponseDto(
                null,
                searchDto.spotifyTrackId(),
                searchDto.title(),
                searchDto.artist(),
                searchDto.album(),
                searchDto.durationMs(),
                searchDto.popularity(),
                searchDto.albumCoverUrl(),
                searchDto.releaseDate()
        );
    }
}
