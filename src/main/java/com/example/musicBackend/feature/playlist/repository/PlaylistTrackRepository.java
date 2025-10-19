package com.example.musicBackend.feature.playlist.repository;

import com.example.musicBackend.feature.playlist.domain.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    List<PlaylistTrack> findByPlaylistIdOrderByPositionAsc(Long playlistId);

    void deleteByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    Optional<PlaylistTrack> findByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    /**
     * 특정 position보다 큰 곡들의 position을 -1 감소시킴(곡 삭제시 position 재정렬)
     *
     * @param playlistId      플레이리스트 ID
     * @param deletedPosition 삭제된 곡의 position
     */
    @Modifying  // UPDATE/DELETE 쿼리를 실행할 때 필요한 어노테이션
    @Query("UPDATE PlaylistTrack pt " +
            "SET pt.position = pt.position - 1 " +
            "WHERE pt.playlist.id = :playlistId " +
            "AND pt.position > :deletedPosition")
    void decrementPositionsAfter(
            @Param("playlistId") Long playlistId,
            @Param("deletedPosition") Integer deletedPosition
    );

}
