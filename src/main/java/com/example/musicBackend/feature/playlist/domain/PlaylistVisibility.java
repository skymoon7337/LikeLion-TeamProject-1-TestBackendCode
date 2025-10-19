package com.example.musicBackend.feature.playlist.domain;

import com.example.musicBackend.feature.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "playlist_visibilities",
        uniqueConstraints = @UniqueConstraint(name = "uk_playlist_visibility_user", columnNames = {"playlist_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistVisibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
