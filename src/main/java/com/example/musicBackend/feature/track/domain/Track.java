package com.example.musicBackend.feature.track.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long trackId;

    @Column(nullable = false)
    private String title;

    private String artist;

    private String album;

    private Integer durationMs;

    private String albumCoverUrl;

    private String releaseDate;

    private LocalDateTime cachedAt;

    private String previewUrl;

    private String primaryGenreName;

    @PrePersist
    protected void onCreate() {
        cachedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        cachedAt = LocalDateTime.now();
    }
}
