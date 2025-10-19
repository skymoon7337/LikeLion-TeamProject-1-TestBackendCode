package com.example.musicBackend.feature.user.service;

import com.example.musicBackend.feature.user.domain.User;
import com.example.musicBackend.feature.user.dto.CreateUserRequestDto;
import com.example.musicBackend.feature.user.dto.UserDetailResponseDto;
import com.example.musicBackend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetailResponseDto createUser(CreateUserRequestDto request) {
        log.info("사용자 생성 - email: {}", request.email());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 생성 완료 - id: {}", savedUser.getId());
        return UserDetailResponseDto.from(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDetailResponseDto> getAllUsers() {
        log.info("모든 사용자 조회");
        return userRepository.findAll().stream()
                .map(UserDetailResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponseDto getUserById(Long userId) {
        log.info("사용자 조회 - id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return UserDetailResponseDto.from(user);
    }

    @Override
    public UserDetailResponseDto updateUser(Long userId, CreateUserRequestDto request) {
        log.info("사용자 정보 수정 - userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.email()).isPresent()) {
                throw new RuntimeException("이미 존재하는 이메일입니다.");
            }
            user.setEmail(request.email());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.nickname() != null) {
            user.setNickname(request.nickname());
        }

        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료");
        return UserDetailResponseDto.from(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("사용자 삭제 - userId: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteById(userId);
        log.info("사용자 삭제 완료");
    }
}