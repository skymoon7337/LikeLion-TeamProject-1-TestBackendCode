package com.example.musicBackend;

import com.example.musicBackend.feature.playlist.domain.Visibility;
import com.example.musicBackend.feature.playlist.dto.AddTrackRequestDto;
import com.example.musicBackend.feature.playlist.dto.PlaylistRequestDto;
import com.example.musicBackend.feature.playlist.dto.PlaylistResponseDto;
import com.example.musicBackend.feature.track.dto.TrackSearchResponseDto;
import com.example.musicBackend.feature.user.dto.AuthResponseDto;
import com.example.musicBackend.feature.user.dto.LoginRequestDto;
import com.example.musicBackend.feature.user.dto.SignupRequestDto;
import com.example.musicBackend.feature.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 통합 테스트 - 전체 사용자 플로우 테스트 (Happy Path Only)
 * 테스트 시나리오:
 * 1. 회원가입
 * 2. 로그인
 * 3. 노래 검색 (Spotify API)
 * 4. 플레이리스트 생성
 * 5. 플레이리스트에 곡 3개 추가
 * 6. 첫 번째 곡을 마지막으로 순서 변경
 * 7. 중간 곡 삭제 후 순서 재정렬 확인
 * 8. 플레이리스트 제목/설명 수정
 * 9. 공개 플레이리스트 목록에서 조회됨
 * 10. 비공개로 변경
 * 11. 공개 목록에 없음 확인
 * 12. 내 플레이리스트 목록에서는 조회됨
 * 13. 다른 사용자는 비공개 플레이리스트 조회 불가
 * 14. 플레이리스트 삭제
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("통합 테스트 - 전체 플로우")
class FlowIntegrationTest {

    private static final String TEST_EMAIL = "e2e-test-" + UUID.randomUUID() + "@example.com";
    private static final String TEST_EMAIL_2 = "e2e-test-2-" + UUID.randomUUID() + "@example.com"; // For permission test

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    // 테스트 간 데이터 공유를 위한 static 변수
    private static Long userId;
    private static Long userId2; // For permission test
    private static Long playlistId;
    private static List<String> spotifyTrackIds = new ArrayList<>();
    private static List<Long> trackIds = new ArrayList<>();

    @Test
    @Order(1)
    @DisplayName("Step 1: 회원가입 성공")
    void step1_회원가입_성공() throws Exception {
        // given
        SignupRequestDto signupRequest = new SignupRequestDto(
                TEST_EMAIL,
                "password123",
                "E2E테스터"
        );

        // when & then
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.nickname").value("E2E테스터"))
                .andReturn();

        // userId 저장
        String responseBody = result.getResponse().getContentAsString();
        AuthResponseDto response = objectMapper.readValue(responseBody, AuthResponseDto.class);
        userId = response.id();

        System.out.println("\n✅ Step 1 완료: 회원가입 성공 (userId: " + userId + ")");
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: 로그인 성공")
    void step2_로그인_성공() throws Exception {
        // given
        LoginRequestDto loginRequest = new LoginRequestDto(
                TEST_EMAIL,
                "password123"
        );

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.nickname").value("E2E테스터"));

        System.out.println("✅ Step 2 완료: 로그인 성공");
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: 노래 검색 - BTS로 검색")
    void step3_노래검색_BTS로_검색() throws Exception {
        // given
        String searchQuery = "BTS";

        // when & then
        MvcResult result = mockMvc.perform(get("/api/spotify/search")
                        .param("q", searchQuery))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].spotifyTrackId").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].artist").exists())
                .andReturn();

        // spotifyTrackId 3개 저장 (곡 추가용)
        String responseBody = result.getResponse().getContentAsString();
        List<TrackSearchResponseDto> tracks = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrackSearchResponseDto.class)
        );

        assertThat(tracks).hasSizeGreaterThanOrEqualTo(3);
        spotifyTrackIds.add(tracks.get(0).spotifyTrackId());
        spotifyTrackIds.add(tracks.get(1).spotifyTrackId());
        spotifyTrackIds.add(tracks.get(2).spotifyTrackId());

        System.out.println("✅ Step 3 완료: 노래 검색 성공 (결과 " + tracks.size() + "개)");
        System.out.println("   - 곡 1: " + tracks.get(0).title() + " - " + tracks.get(0).artist());
        System.out.println("   - 곡 2: " + tracks.get(1).title() + " - " + tracks.get(1).artist());
        System.out.println("   - 곡 3: " + tracks.get(2).title() + " - " + tracks.get(2).artist());
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: 플레이리스트 생성 - 공개 설정")
    void step4_플레이리스트_생성_공개설정() throws Exception {
        // given
        PlaylistRequestDto playlistRequest = new PlaylistRequestDto(
                "E2E 테스트 플레이리스트",
                "통합 테스트용 플레이리스트입니다",
                null,
                Visibility.PUBLIC,
                null
        );

        // when & then
        MvcResult result = mockMvc.perform(post("/api/playlists")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playlistRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("E2E 테스트 플레이리스트"))
                .andExpect(jsonPath("$.description").value("통합 테스트용 플레이리스트입니다"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andReturn();

        // playlistId 저장
        String responseBody = result.getResponse().getContentAsString();
        PlaylistResponseDto response = objectMapper.readValue(responseBody, PlaylistResponseDto.class);
        playlistId = response.id();

        System.out.println("✅ Step 4 완료: 플레이리스트 생성 (playlistId: " + playlistId + ")");
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: 플레이리스트에 곡 3개 추가")
    void step5_플레이리스트에_곡_3개_추가() throws Exception {
        // 곡 3개 추가
        for (int i = 0; i < 3; i++) {
            AddTrackRequestDto addTrackRequest = new AddTrackRequestDto(spotifyTrackIds.get(i));

            MvcResult result = mockMvc.perform(post("/api/playlists/{playlistId}/tracks", playlistId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addTrackRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tracks").isArray())
                    .andReturn();

            // 마지막 추가 후 trackId 저장
            if (i == 2) {
                String responseBody = result.getResponse().getContentAsString();
                PlaylistResponseDto response = objectMapper.readValue(responseBody, PlaylistResponseDto.class);

                assertThat(response.tracks()).hasSize(3);

                // trackId 저장 (순서 변경, 삭제용)
                for (var track : response.tracks()) {
                    trackIds.add(track.id());
                }

                // position 확인
                assertThat(response.tracks().get(0).position()).isEqualTo(0);
                assertThat(response.tracks().get(1).position()).isEqualTo(1);
                assertThat(response.tracks().get(2).position()).isEqualTo(2);
            }
        }

        System.out.println("✅ Step 5 완료: 곡 3개 추가 (position: 0, 1, 2)");
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: 첫 번째 곡을 마지막으로 이동")
    void step6_첫번째_곡을_마지막으로_이동() throws Exception {
        // given - 첫 번째 곡(position 0)을 마지막(position 2)으로
        Long firstTrackId = trackIds.get(0);

        // when & then
        MvcResult result = mockMvc.perform(put("/api/playlists/{playlistId}/tracks/{trackId}/position", playlistId, firstTrackId)
                        .param("userId", userId.toString())
                        .param("newPosition", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks").isArray())
                .andReturn();

        // 순서 확인: [원래2번째, 원래3번째, 원래1번째]
        String responseBody = result.getResponse().getContentAsString();
        PlaylistResponseDto response = objectMapper.readValue(responseBody, PlaylistResponseDto.class);

        assertThat(response.tracks()).hasSize(3);
        assertThat(response.tracks().get(0).id()).isEqualTo(trackIds.get(1)); // 원래 2번째
        assertThat(response.tracks().get(1).id()).isEqualTo(trackIds.get(2)); // 원래 3번째
        assertThat(response.tracks().get(2).id()).isEqualTo(trackIds.get(0)); // 원래 1번째

        // position도 0, 1, 2로 올바르게 설정되었는지
        assertThat(response.tracks().get(0).position()).isEqualTo(0);
        assertThat(response.tracks().get(1).position()).isEqualTo(1);
        assertThat(response.tracks().get(2).position()).isEqualTo(2);

        System.out.println("✅ Step 6 완료: 순서 변경 성공 (0→2)");
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: 중간 곡 삭제 및 순서 재정렬 확인")
    void step7_중간_곡_삭제() throws Exception {
        // given - 삭제 전 상태 저장
        MvcResult beforeResult = mockMvc.perform(get("/api/playlists/{playlistId}", playlistId))
                .andExpect(status().isOk())
                .andReturn();

        PlaylistResponseDto beforePlaylist = objectMapper.readValue(
                beforeResult.getResponse().getContentAsString(),
                PlaylistResponseDto.class
        );

        int beforeSize = beforePlaylist.tracks().size();
        Long middleTrackId = beforePlaylist.tracks().get(1).id(); // position 1 곡
        Long lastTrackId = beforePlaylist.tracks().get(2).id(); // position 2 곡

        System.out.println("🗑️ 삭제 전:");
        System.out.println("  총 곡 수: " + beforeSize);
        System.out.println("  Position 1 곡: " + middleTrackId + " ← 삭제 예정");
        System.out.println("  Position 2 곡: " + lastTrackId + " ← position 1로 이동 예상");

        // when - 중간 곡(position 1) 삭제
        mockMvc.perform(delete("/api/playlists/{playlistId}/tracks/{trackId}",
                        playlistId, middleTrackId)
                        .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // then - 삭제 후 확인
        MvcResult afterResult = mockMvc.perform(get("/api/playlists/{playlistId}", playlistId))
                .andExpect(status().isOk())
                .andReturn();

        PlaylistResponseDto afterPlaylist = objectMapper.readValue(
                afterResult.getResponse().getContentAsString(),
                PlaylistResponseDto.class
        );

        // 1. 개수 확인
        assertThat(afterPlaylist.tracks()).hasSize(beforeSize - 1);

        // 2. 삭제된 곡 없음 확인
        assertThat(afterPlaylist.tracks())
                .noneMatch(track -> track.id().equals(middleTrackId));

        // 3. Position 연속성 확인 (가장 중요!)
        for (int i = 0; i < afterPlaylist.tracks().size(); i++) {
            assertThat(afterPlaylist.tracks().get(i).position())
                    .as("Track at index %d should have position %d", i, i)
                    .isEqualTo(i);
        }

        // 4. 마지막 곡이 position 1로 이동했는지 확인
        assertThat(afterPlaylist.tracks().get(1).id()).isEqualTo(lastTrackId);
        assertThat(afterPlaylist.tracks().get(1).position()).isEqualTo(1);

        System.out.println("✅ Step 7 완료:");
        System.out.println("  총 곡 수: " + beforeSize + " → " + afterPlaylist.tracks().size());
        System.out.println("  Position 재정렬 확인: ✅");
        afterPlaylist.tracks().forEach(track ->
                System.out.println("    - Position " + track.position() + ": Track " + track.id())
        );
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: 플레이리스트 제목/설명 수정")
    void step8_플레이리스트_제목_설명_수정() throws Exception {
        // given
        PlaylistRequestDto updateRequest = new PlaylistRequestDto(
                "수정된 E2E 플레이리스트",
                "제목과 설명이 수정되었습니다",
                null,
                null,
                null
        );

        // when & then
        mockMvc.perform(put("/api/playlists/{playlistId}", playlistId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playlistId))
                .andExpect(jsonPath("$.title").value("수정된 E2E 플레이리스트"))
                .andExpect(jsonPath("$.description").value("제목과 설명이 수정되었습니다"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC")); // 여전히 PUBLIC

        System.out.println("✅ Step 8 완료: 플레이리스트 정보 수정");
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: 공개 플레이리스트 목록에서 조회됨")
    void step9_공개_플레이리스트_목록에서_조회됨() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(get("/api/playlists/public"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // 내 플레이리스트가 목록에 포함되어 있는지 확인
        String responseBody = result.getResponse().getContentAsString();
        List<PlaylistResponseDto> playlists = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PlaylistResponseDto.class)
        );

        boolean myPlaylistExists = playlists.stream()
                .anyMatch(p -> p.id().equals(playlistId) && p.visibility() == Visibility.PUBLIC);
        assertThat(myPlaylistExists).isTrue();

        System.out.println("✅ Step 9 완료: 공개 목록에 포함됨 확인");
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: 비공개로 변경")
    void step10_비공개로_변경() throws Exception {
        // given
        PlaylistRequestDto updateRequest = new PlaylistRequestDto(
                null,
                null,
                null,
                Visibility.PRIVATE,
                null
        );

        // when & then
        mockMvc.perform(put("/api/playlists/{playlistId}", playlistId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playlistId))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.title").value("수정된 E2E 플레이리스트")); // 제목은 그대로

        System.out.println("✅ Step 10 완료: 비공개로 변경 (PUBLIC→PRIVATE)");
    }

    @Test
    @Order(11)
    @DisplayName("Step 11: 공개 목록에 없음 확인")
    void step11_공개_목록에_없음() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(get("/api/playlists/public"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // 내 플레이리스트가 목록에 없어야 함
        String responseBody = result.getResponse().getContentAsString();
        List<PlaylistResponseDto> playlists = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PlaylistResponseDto.class)
        );

        boolean myPlaylistExists = playlists.stream()
                .anyMatch(p -> p.id().equals(playlistId));
        assertThat(myPlaylistExists).isFalse();

        System.out.println("✅ Step 11 완료: 공개 목록에 없음 (PRIVATE이므로)");
    }

    @Test
    @Order(12)
    @DisplayName("Step 12: 내 플레이리스트 목록에서는 조회됨")
    void step12_내_플레이리스트_목록에서는_조회됨() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(get("/api/playlists/user/{userId}", userId)) // '내 목록' API (userId를 경로 변수로 사용)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // 비공개로 전환한 내 플레이리스트가 목록에 포함되어 있는지 확인
        String responseBody = result.getResponse().getContentAsString();
        List<PlaylistResponseDto> playlists = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PlaylistResponseDto.class)
        );

        boolean myPlaylistExists = playlists.stream()
                .anyMatch(p -> p.id().equals(playlistId) && p.visibility() == Visibility.PRIVATE);
        assertThat(myPlaylistExists).isTrue();

        System.out.println("✅ Step 12 완료: 내 플레이리스트 목록에 포함됨 확인 (PRIVATE 상태)");
    }

    @Disabled("보안 기능 구현 후 활성화 예정")
    @Test
    @Order(13)
    @DisplayName("Step 13: 다른 사용자는 비공개 플레이리스트 조회 불가")
    void step13_다른사용자_비공개_플레이리스트_접근불가() throws Exception {
        // given - 다른 사용자 생성
        SignupRequestDto signupRequest = new SignupRequestDto(TEST_EMAIL_2, "password456", "침입테스터");
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();
        userId2 = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponseDto.class).id();
        System.out.println("   - 다른 사용자 생성 완료 (userId2: " + userId2 + ")");


        // when & then - 다른 사용자로 비공개 플레이리스트 조회 시도
        System.out.println("   - User " + userId2 + "가 User " + userId + "의 private playlist " + playlistId + " 조회를 시도합니다.");
        mockMvc.perform(get("/api/playlists/{playlistId}", playlistId)
                        .param("userId", userId2.toString())) // 다른 사용자의 ID로 조회 시도
                .andDo(print())
                .andExpect(status().is5xxServerError()); // 혹은 isNotFound() (404)가 더 적절함

        System.out.println("✅ Step 13 완료: 다른 사용자의 비공개 플레이리스트 접근 실패 확인");
    }

    @Test
    @Order(14)
    @DisplayName("Step 14: 플레이리스트 삭제")
    void step14_플레이리스트_삭제() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/playlists/{playlistId}", playlistId)
                        .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 삭제 후 조회 시도 - 404 또는 500 예상
        mockMvc.perform(get("/api/playlists/{playlistId}", playlistId))
                .andDo(print())
                .andExpect(status().is5xxServerError()); // 현재는 500 (나중에 404로 개선)

        System.out.println("✅ Step 14 완료: 플레이리스트 삭제");
        System.out.println("\n🎉 E2E 통합 테스트 전체 완료!");
    }

    @AfterAll
    void cleanupUser() {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        userRepository.findByEmail(TEST_EMAIL_2).ifPresent(userRepository::delete);
    }
}
