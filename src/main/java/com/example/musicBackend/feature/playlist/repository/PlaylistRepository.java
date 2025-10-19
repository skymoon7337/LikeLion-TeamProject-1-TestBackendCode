package com.example.musicBackend.feature.playlist.repository;

import com.example.musicBackend.feature.playlist.domain.Playlist;
import com.example.musicBackend.feature.playlist.domain.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserId(Long userId);

    List<Playlist> findByVisibility(Visibility visibility);

    /**
     * 플레이리스트를 tracks와 함께 조회 (Fetch Join)
     * Lazy Loading 문제 해결을 위해 한 번에 모든 데이터를 가져옴
     */
    @Query("SELECT p FROM Playlist p " +
            "LEFT JOIN FETCH p.playlistTracks pt " +
            "LEFT JOIN FETCH pt.track " +
            "WHERE p.id = :id")
    Optional<Playlist> findByIdWithTracks(@Param("id") Long id);
}
