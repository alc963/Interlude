package com.naugroup3.interlude.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.naugroup3.interlude.exceptions.TokenRefreshException;
import com.naugroup3.interlude.models.RefreshToken;
import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.repositories.RefreshTokenRepository;
import com.naugroup3.interlude.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refresh_token_repository;
    @Mock
    private UserRepository user_repository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refresh_token_repository, user_repository, 60_000L);
    }

    @Test
    void create_refresh_token_replaces_existing_token_and_uses_configured_lifetime() {
        User user = new User();
        user.setId(7L);
        when(user_repository.findById(7L)).thenReturn(Optional.of(user));
        when(refresh_token_repository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = service.createRefreshToken(7L);

        assertThat(token.getUser()).isSameAs(user);
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getExpiryDate()).isAfter(Instant.now().plusSeconds(55));
        verify(refresh_token_repository).deleteByUser(user);
    }

    @Test
    void verify_expiration_deletes_expired_token_and_rejects_it() {
        RefreshToken token = new RefreshToken();
        token.setToken("expired-token");
        token.setExpiryDate(Instant.now().minusMillis(1));

        assertThatThrownBy(() -> service.verifyExpiration(token))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessage("Refresh token expired; please sign in again");
        verify(refresh_token_repository).delete(token);
    }

    @Test
    void verify_expiration_accepts_unexpired_token() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusSeconds(60));

        assertThat(service.verifyExpiration(token)).isSameAs(token);
    }
}
