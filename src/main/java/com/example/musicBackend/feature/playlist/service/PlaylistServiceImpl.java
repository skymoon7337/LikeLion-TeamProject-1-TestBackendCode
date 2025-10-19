package com.example.musicBackend.feature.playlist.service;

import com.example.musicBackend.external.spotify.service.SpotifyService;
import com.example.musicBackend.feature.playlist.domain.Playlist;
import com.example.musicBackend.feature.playlist.domain.PlaylistTrack;
import com.example.musicBackend.feature.playlist.domain.PlaylistVisibility;
import com.example.musicBackend.feature.playlist.domain.Visibility;
import com.example.musicBackend.feature.playlist.dto.PlaylistRequestDto;
import com.example.musicBackend.feature.playlist.dto.PlaylistResponseDto;
import com.example.musicBackend.feature.playlist.repository.PlaylistRepository;
import com.example.musicBackend.feature.playlist.repository.PlaylistTrackRepository;
import com.example.musicBackend.feature.playlist.repository.PlaylistVisibilityRepository;
import com.example.musicBackend.feature.track.domain.Track;
import com.example.musicBackend.feature.user.domain.User;
import com.example.musicBackend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final PlaylistVisibilityRepository playlistVisibilityRepository;
    private final UserRepository userRepository;
    private final SpotifyService spotifyService;

    /**
     * 플레이리스트 생성
     */
    @Override
    @Transactional
    public PlaylistResponseDto createPlaylist(Long userId, PlaylistRequestDto request) {
        log.info("플레이리스트 생성 - userId: {}, title: {}", userId, request.title());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Playlist playlist = Playlist.builder()
                .title(request.title())
                .description(request.description())
                .coverImageUrl(request.coverImageUrl())
                .visibility(request.visibility() != null ? request.visibility() : Visibility.PUBLIC)
                .user(user)
                .build();

        Playlist savedPlaylist = playlistRepository.save(playlist);
        syncSharedUsers(savedPlaylist, savedPlaylist.getVisibility(), request.sharedUserIds(), true);
        log.info("플레이리스트 생성 완료 - id: {}", savedPlaylist.getId());

        Playlist reloaded = playlistRepository.findById(savedPlaylist.getId())
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));
        return PlaylistResponseDto.from(reloaded);
    }

    /**
     * 특정 사용자의 플레이리스트 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponseDto> getUserPlaylists(Long userId) {
        return playlistRepository.findByUserId(userId).stream()
                .map(PlaylistResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 공개 플레이리스트 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponseDto> getPublicPlaylists() {
        return playlistRepository.findByVisibility(Visibility.PUBLIC).stream()
                .map(PlaylistResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 플레이리스트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public PlaylistResponseDto getPlaylist(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));
        return PlaylistResponseDto.from(playlist);
    }

    /**
     * 플레이리스트 정보 수정
     */
    @Override
    @Transactional
    public PlaylistResponseDto updatePlaylist(Long playlistId, Long userId, PlaylistRequestDto request) {
        log.info("✏플레이리스트 수정 - playlistId: {}, userId: {}", playlistId, userId);

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));

        // 권한 확인
        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("플레이리스트를 수정할 권한이 없습니다.");
        }

        if (request.title() != null) {
            playlist.setTitle(request.title());
        }
        if (request.description() != null) {
            playlist.setDescription(request.description());
        }
        if (request.coverImageUrl() != null) {
            playlist.setCoverImageUrl(request.coverImageUrl());
        }
        if (request.visibility() != null) {
            playlist.setVisibility(request.visibility());
        }

        Playlist updatedPlaylist = playlistRepository.save(playlist);
        boolean shouldUpdateSharedUsers = updatedPlaylist.getVisibility() != Visibility.SHARED
                || request.sharedUserIds() != null;
        syncSharedUsers(updatedPlaylist, updatedPlaylist.getVisibility(), request.sharedUserIds(), shouldUpdateSharedUsers);
        log.info("플레이리스트 수정 완료");

        Playlist reloaded = playlistRepository.findById(updatedPlaylist.getId())
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));
        return PlaylistResponseDto.from(reloaded);
    }

    /**
     * 플레이리스트 삭제
     */
    @Override
    @Transactional
    public void deletePlaylist(Long playlistId, Long userId) {
        log.info("🗑플레이리스트 삭제 - playlistId: {}, userId: {}", playlistId, userId);

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));

        // 권한 확인
        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("플레이리스트를 삭제할 권한이 없습니다.");
        }

        playlistRepository.delete(playlist);
        log.info("플레이리스트 삭제 완료");
    }

    /**
     * 플레이리스트에 곡 추가
     */
    @Override
    @Transactional
    public PlaylistResponseDto addTrackToPlaylist(Long playlistId, Long userId, String spotifyTrackId) {
        log.info("트랙 추가 - playlistId: {}, spotifyTrackId: {}", playlistId, spotifyTrackId);

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));

        // 권한 확인
        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("플레이리스트를 수정할 권한이 없습니다.");
        }

        Track track = spotifyService.getOrCreateTrackEntity(spotifyTrackId);

        // 중복 체크
        boolean alreadyExists = playlistTrackRepository
                .findByPlaylistIdAndTrackId(playlistId, track.getId())
                .isPresent();

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 플레이리스트에 추가된 곡입니다.");
        }

        // 다음 position 계산
        int nextPosition = playlist.getPlaylistTracks().size();

        PlaylistTrack playlistTrack = PlaylistTrack.builder()
                .playlist(playlist)
                .track(track)
                .position(nextPosition)
                .build();

//1 : DB에 playlistTrack을 저장했지만, 메모리 상의 playlist 객체 컬렉션은 자동으로 갱신되지 않음 (JPA 영속성 컨텍스트 및 양방향 관계 관리 특성)
        playlistTrackRepository.save(playlistTrack);
//3 : 메모리(JPA 영속성 컨테이너)상에 있는 playlist 객체로부터 PlaylistTrack들의 컬렉션(List)을 가져옴
//3 : 위에서 DB에 저장한 새로운 playlistTrack 객체를 메모리(JPA 영속성 컨테이너)에 저장함 (양방향 관계의 일관성을 유지)
        playlist.getPlaylistTracks().add(playlistTrack);
        log.info("곡 추가 완료");

//2 : 새로 고침해서 반환한다. tracks까지 한 번에 조회해서 Lazy Loading 문제 해결
        playlist = playlistRepository.findByIdWithTracks(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));
        return PlaylistResponseDto.from(playlist);
    }

    /**
     * 플레이리스트에서 곡 제거
     */
    @Override
    @Transactional
    public void removeTrackFromPlaylist(Long playlistId, Long userId, Long trackId) {
        log.info("트랙 제거 - playlistId: {}, trackId: {}", playlistId, trackId);

        // 1. 플레이리스트 조회
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));

        // 2. 권한 확인
        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("플레이리스트를 수정할 권한이 없습니다.");
        }

        // 3. 삭제할 곡의 현재 position 조회 (재정렬에 필요)
        PlaylistTrack playlistTrack = playlistTrackRepository
                .findByPlaylistIdAndTrackId(playlistId, trackId)
                .orElseThrow(() -> new RuntimeException("플레이리스트에 해당 곡이 없습니다."));

        Integer deletedPosition = playlistTrack.getPosition();
        log.info("삭제할 곡의 position: {}", deletedPosition);

        // 4. 곡 삭제
        playlistTrackRepository.deleteByPlaylistIdAndTrackId(playlistId, trackId);
        log.info("곡 삭제 완료");

        // 5. 삭제된 position보다 큰 곡들의 position을 -1 감소
        playlistTrackRepository.decrementPositionsAfter(playlistId, deletedPosition);
        log.info("순서 재정렬 완료 (position > {} 인 곡들 -1)", deletedPosition);
    }

    /**
     * 플레이리스트 내 곡 순서 변경
     */
    @Override
    @Transactional
    public PlaylistResponseDto updateTrackPosition(Long playlistId, Long userId, Long trackId, Integer newPosition) {
        log.info("트랙 순서 변경 - playlistId: {}, trackId: {}, newPosition: {}", playlistId, trackId, newPosition);

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("플레이리스트를 찾을 수 없습니다."));

        // 권한 확인
        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("플레이리스트를 수정할 권한이 없습니다.");
        }

        PlaylistTrack playlistTrack = playlistTrackRepository.findByPlaylistIdAndTrackId(playlistId, trackId)
                .orElseThrow(() -> new RuntimeException("플레이리스트에 해당 곡이 없습니다."));

        int oldPosition = playlistTrack.getPosition();

        // 같은 위치면 아무것도 안 함
        if (oldPosition == newPosition) {
            return PlaylistResponseDto.from(playlist);
        }

        // 포지션 맞교환시 db에 저장할때 유니크 오류때문에 임시 해결책 (이동할 곡을 임시 position으로 이동)
        playlistTrack.setPosition(-1);
        playlistTrackRepository.save(playlistTrack);
        playlistTrackRepository.flush();  // 즉시 DB 반영

        // 순서 재조정
        if (newPosition < oldPosition) {
            // 위로 이동
            List<PlaylistTrack> tracksToShift = playlist.getPlaylistTracks().stream()
                    .filter(ps -> ps.getPosition() >= newPosition && ps.getPosition() < oldPosition)
                    .collect(Collectors.toList());

            for (PlaylistTrack ps : tracksToShift) {
                ps.setPosition(ps.getPosition() + 1);
            }

            playlistTrackRepository.saveAll(tracksToShift);
            playlistTrackRepository.flush();  // 즉시 DB 반영

        } else {
            // 아래로 이동
            List<PlaylistTrack> tracksToShift = playlist.getPlaylistTracks().stream()
                    .filter(ps -> ps.getPosition() > oldPosition && ps.getPosition() <= newPosition)
                    .collect(Collectors.toList());

            for (PlaylistTrack ps : tracksToShift) {
                ps.setPosition(ps.getPosition() - 1);
            }

            playlistTrackRepository.saveAll(tracksToShift);
            playlistTrackRepository.flush();  // 즉시 DB 반영
        }

        playlistTrack.setPosition(newPosition);
        playlistTrackRepository.save(playlistTrack); // 위에서 saveAll, flush 했으니 이동할 곡만 저장

        log.info("곡 순서 변경 완료");

        // 새로 고침해서 반환
        playlist = playlistRepository.findById(playlistId).get();
        return PlaylistResponseDto.from(playlist);
    }

    private void syncSharedUsers(Playlist playlist, Visibility visibility, List<Long> sharedUserIds, boolean shouldUpdateList) {
        List<PlaylistVisibility> current = new ArrayList<>(playlist.getPlaylistVisibilities());

        if (visibility != Visibility.SHARED) {
            if (!current.isEmpty()) {
                playlistVisibilityRepository.deleteAll(current);
                playlist.getPlaylistVisibilities().clear();
            }
            return;
        }

        if (!shouldUpdateList) {
            return;
        }

        Set<Long> desired = new HashSet<>(sharedUserIds != null ? sharedUserIds : Collections.emptyList());
        // 소유자는 자동 접근 가능하므로 제외
        desired.remove(playlist.getUser().getId());

        for (PlaylistVisibility pv : current) {
            Long userId = pv.getUser().getId();
            if (!desired.contains(userId)) {
                playlistVisibilityRepository.delete(pv);
                playlist.getPlaylistVisibilities().remove(pv);
            } else {
                desired.remove(userId);
            }
        }

        for (Long userId : desired) {
            User sharedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("공유 대상 사용자를 찾을 수 없습니다."));
            PlaylistVisibility visibilityEntry = PlaylistVisibility.builder()
                    .playlist(playlist)
                    .user(sharedUser)
                    .build();
            playlist.getPlaylistVisibilities().add(visibilityEntry);
            playlistVisibilityRepository.save(visibilityEntry);
        }
    }
}
