package com.example.musicBackend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 애플리케이션 기본 설정
 */
@Configuration
public class AppConfig {

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder
     * Spring Security의 BCrypt 사용 (Security 기능은 application.properties에서 비활성화)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebClient webClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().jackson2JsonDecoder(
                        new Jackson2JsonDecoder(new ObjectMapper(), new MediaType("text", "javascript"))
                ))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
