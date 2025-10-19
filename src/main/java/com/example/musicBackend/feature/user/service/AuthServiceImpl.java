package com.example.musicBackend.feature.user.service;

import com.example.musicBackend.feature.user.domain.User;
import com.example.musicBackend.feature.user.dto.LoginRequestDto;
import com.example.musicBackend.feature.user.dto.SignupRequestDto;
import com.example.musicBackend.feature.user.dto.AuthResponseDto;
import com.example.musicBackend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDto signup(SignupRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        userRepository.save(user);

        return new AuthResponseDto(user.getId(), user.getEmail(), user.getNickname());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return new AuthResponseDto(user.getId(), user.getEmail(), user.getNickname());
    }
}
