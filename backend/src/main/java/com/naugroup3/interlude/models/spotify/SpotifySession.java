package com.naugroup3.interlude.models.spotify;

import java.time.Duration;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "spotify_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpotifySession {
    @Id
    private Long user_id;

    @Column(length = 1024, nullable = false)
    private String access_token;
    @Column(length = 1024, nullable = false)
    private String refresh_token;
    @Column(length = 500)
    private String scope;
    @Column(nullable = false)
    private Instant expires_at;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant created_at;
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant modified_at;

    public SpotifySession(Long user_id, SpotifyTokenResponse tokens, Instant now) {
        this.user_id = user_id;
        apply_tokens(tokens, now);
    }

    public void apply_tokens(SpotifyTokenResponse tokens, Instant now) {
        this.access_token = tokens.accessToken();
        if (tokens.refreshToken() != null)
            this.refresh_token = tokens.refreshToken();
        if (tokens.scope() != null)
            this.scope = tokens.scope();
        this.expires_at = now.plusSeconds(tokens.expiresIn());
    }

    public boolean expires_within(Duration margin, Instant now) {
        return !now.plus(margin).isBefore(expires_at);
    }
}
