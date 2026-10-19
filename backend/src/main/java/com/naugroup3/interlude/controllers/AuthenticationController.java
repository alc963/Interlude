package com.naugroup3.interlude.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naugroup3.interlude.utils.jwt.JWTUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    public record LoginRequest(String email, String password) {}

    private final AuthenticationManager authentication_manager;
    private final JWTUtils jwt_utils;

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody LoginRequest request) {
        final UsernamePasswordAuthenticationToken auth_token = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        authentication_manager.authenticate(auth_token);
        
        final String token = jwt_utils.generate_token(request.email());
        return ResponseEntity.ok(token);
    }
}