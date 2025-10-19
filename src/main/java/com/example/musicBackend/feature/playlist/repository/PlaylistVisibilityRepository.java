package com.example.musicBackend.feature.playlist.repository;

import com.example.musicBackend.feature.playlist.domain.Playlist;
import com.example.musicBackend.feature.playlist.domain.PlaylistVisibility;
import com.example.musicBackend.feature.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistVisibilityRepository extends JpaRepository<PlaylistVisibility, Long> {

    List<PlaylistVisibility> findByPlaylist(Playlist playlist);

    Optional<PlaylistVisibility> findByPlaylistAndUser(Playlist playlist, User user);

    void deleteByPlaylistAndUser(Playlist playlist, User user);
}
