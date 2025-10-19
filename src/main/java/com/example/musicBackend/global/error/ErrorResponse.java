package com.example.musicBackend.global.error;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * API 에러 응답 표준 형식
 */
public record ErrorResponse(
        String error,
        String message,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
    /**
     * 에러 응답 생성 (timestamp 자동 설정)
     */
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, LocalDateTime.now());
    }
}