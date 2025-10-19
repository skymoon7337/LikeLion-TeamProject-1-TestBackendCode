package com.example.musicBackend.feature.track.repository;

import com.example.musicBackend.feature.track.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findBySpotifyTrackId(String spotifyTrackId);
}
