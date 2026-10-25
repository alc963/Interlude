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
import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.repositories.UserRepository;
import com.naugroup3.interlude.services.UsernameService;
import com.naugroup3.interlude.utils.jwt.JWTUtils;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock private UserRepository user_repository;
    @Mock private UsernameService username_service;
    @Mock private PasswordEncoder password_encoder;
    @Mock private AuthenticationManager authentication_manager;
    @Mock private JWTUtils jwt_utils;

    private AuthenticationController controller;
    private final SignupRequest signup = new SignupRequest("sumi", "Sumi@Example.com", "password123");

    @BeforeEach
    void setUp() {
        controller = new AuthenticationController(user_repository, username_service, password_encoder, authentication_manager, jwt_utils);
    }

    private static void assert_response(ResponseEntity<String> response, HttpStatus status, String body) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isEqualTo(body);
    }

    @Test
    void signup_creates_user_and_returns_token() {
        when(password_encoder.encode("password123")).thenReturn("hashed");
        when(jwt_utils.generate_token("sumi@example.com")).thenReturn("jwt-token");

        assert_response(controller.signup(signup), HttpStatus.CREATED, "jwt-token");

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
        when(jwt_utils.generate_token("sumi@example.com")).thenReturn("jwt-token");
        assert_response(controller.authenticate(new LoginRequest("sumi@example.com", "password123")), HttpStatus.OK, "jwt-token");
    }

    @Test
    void login_is_unauthorized_for_bad_credentials() {
        when(authentication_manager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        assert_response(controller.authenticate(new LoginRequest("sumi@example.com", "wrong-password")),
            HttpStatus.UNAUTHORIZED, "Invalid email or password");
        verify(jwt_utils, never()).generate_token(any());
    }

    @Test
    void username_check_reports_existence_and_rejects_invalid_names() {
        when(username_service.exists("sumi")).thenReturn(true);

        assertThat(controller.username_check("sumi").exists()).isTrue();
        assertThatThrownBy(() -> controller.username_check("b#"))
            .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
