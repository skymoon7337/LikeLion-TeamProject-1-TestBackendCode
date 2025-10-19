package com.example.musicBackend.feature.user.controller;

import com.example.musicBackend.feature.user.dto.CreateUserRequestDto;
import com.example.musicBackend.feature.user.dto.UserDetailResponseDto;
import com.example.musicBackend.feature.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사용자 관리 API
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 생성 (회원가입)
     */
    @PostMapping
    public ResponseEntity<UserDetailResponseDto> createUser(@RequestBody CreateUserRequestDto request) {
        UserDetailResponseDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * 모든 사용자 조회
     */
    @GetMapping
    public ResponseEntity<List<UserDetailResponseDto>> getAllUsers() {
        List<UserDetailResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 사용자 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponseDto> getUser(@PathVariable Long userId) {
        UserDetailResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserDetailResponseDto> updateUser(
            @PathVariable Long userId,
            @RequestBody CreateUserRequestDto request) {
        UserDetailResponseDto user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}