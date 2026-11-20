package com.naugroup3.interlude.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.controllers.AuthenticationController.LoginRequest;
import com.naugroup3.interlude.controllers.AuthenticationController.SignupRequest;
import com.naugroup3.interlude.controllers.AuthenticationController.TokenRefreshRequest;
import com.naugroup3.interlude.controllers.AuthenticationController.TokenResponse;
import com.naugroup3.interlude.exceptions.TokenRefreshException;
import com.naugroup3.interlude.models.RefreshToken;
import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.repositories.UserRepository;
import com.naugroup3.interlude.services.RefreshTokenService;
import com.naugroup3.interlude.services.UsernameService;
import com.naugroup3.interlude.utils.jwt.JWTUtils;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock
    private UserRepository user_repository;
    @Mock
    private UsernameService username_service;
    @Mock
    private PasswordEncoder password_encoder;
    @Mock
    private AuthenticationManager authentication_manager;
    @Mock
    private JWTUtils jwt_utils;
    @Mock
    private RefreshTokenService refresh_token_service;

    private AuthenticationController controller;
    private final SignupRequest signup = new SignupRequest("sumi", "Sumi@Example.com", "password123");

    @BeforeEach
    void setUp() {
        controller = new AuthenticationController(user_repository, username_service, password_encoder,
                authentication_manager, jwt_utils, refresh_token_service);
    }

    private static void assert_response(ResponseEntity<?> response, HttpStatus status, String body) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isEqualTo(body);
    }

    private static User user() {
        User user = new User();
        user.setId(7L);
        user.setUsername("sumi");
        user.setEmail("sumi@example.com");
        return user;
    }

    private static RefreshToken refresh_token(User user, String token) {
        RefreshToken refresh_token = new RefreshToken();
        refresh_token.setUser(user);
        refresh_token.setToken(token);
        return refresh_token;
    }

    @Test
    void signup_creates_user_and_returns_token_pair() {
        when(password_encoder.encode("password123")).thenReturn("hashed");
        when(user_repository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });
        when(jwt_utils.generate_token("sumi@example.com")).thenReturn("jwt-token");
        when(refresh_token_service.createRefreshToken(7L)).thenReturn(refresh_token(user(), "refresh-token"));

        ResponseEntity<?> response = controller.signup(signup);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(new TokenResponse("jwt-token", "Bearer", "refresh-token"));

        final ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(user_repository).save(saved.capture());
        assertThat(saved.getValue()).extracting(User::getUsername, User::getEmail, User::getPassword_hash)
                .containsExactly("sumi", "sumi@example.com", "hashed");
        verify(username_service).insert("sumi");
    }

    @Test
    void signup_conflicts_on_used_email_or_username() {
        when(user_repository.existsByEmailIgnoreCase("Sumi@Example.com")).thenReturn(true);
        assert_response(controller.signup(signup), HttpStatus.CONFLICT, "Email already used");

        when(user_repository.existsByEmailIgnoreCase("Sumi@Example.com")).thenReturn(false);
        when(username_service.exists("sumi")).thenReturn(true);
        assert_response(controller.signup(signup), HttpStatus.CONFLICT, "Username already used");

        verify(user_repository, never()).save(any());
    }

    @Test
    void signup_conflicts_on_save_race() {
        when(user_repository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assert_response(controller.signup(signup), HttpStatus.CONFLICT, "Email or username already used");
        verify(username_service, never()).insert(any());
        verify(jwt_utils, never()).generate_token(any());
    }

    @Test
    void login_returns_token_for_valid_credentials() {
        User user = user();
        when(user_repository.findByEmail("sumi@example.com")).thenReturn(java.util.Optional.of(user));
        when(jwt_utils.generate_token("sumi@example.com")).thenReturn("jwt-token");
        when(refresh_token_service.createRefreshToken(7L)).thenReturn(refresh_token(user, "refresh-token"));

        ResponseEntity<?> response = controller.authenticate(new LoginRequest("sumi@example.com", "password123"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new TokenResponse("jwt-token", "Bearer", "refresh-token"));
    }

    @Test
    void login_is_unauthorized_for_bad_credentials() {
        when(authentication_manager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        assert_response(controller.authenticate(new LoginRequest("sumi@example.com", "wrong-password")),
                HttpStatus.UNAUTHORIZED, "Invalid email or password");
        verify(jwt_utils, never()).generate_token(any());
        verify(refresh_token_service, never()).createRefreshToken(any());
    }

    @Test
    void refresh_returns_new_access_token_and_keeps_refresh_token() {
        User user = user();
        RefreshToken refresh_token = refresh_token(user, "refresh-token");
        when(refresh_token_service.findByToken("refresh-token")).thenReturn(java.util.Optional.of(refresh_token));
        when(refresh_token_service.verifyExpiration(refresh_token)).thenReturn(refresh_token);
        when(jwt_utils.generate_token("sumi@example.com")).thenReturn("new-jwt-token");

        ResponseEntity<?> response = controller.refresh_token(new TokenRefreshRequest("refresh-token"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new TokenResponse("new-jwt-token", "Bearer", "refresh-token"));
    }

    @Test
    void refresh_rejects_unknown_refresh_token() {
        when(refresh_token_service.findByToken("unknown")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> controller.refresh_token(new TokenRefreshRequest("unknown")))
                .isInstanceOf(TokenRefreshException.class);
    }

    @Test
    void username_check_reports_existence_and_rejects_invalid_names() {
        when(username_service.exists("sumi")).thenReturn(true);

        assertThat(controller.username_check("sumi").exists()).isTrue();
        assertThatThrownBy(() -> controller.username_check("b#"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
