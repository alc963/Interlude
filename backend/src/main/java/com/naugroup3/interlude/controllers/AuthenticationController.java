package com.naugroup3.interlude.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.repositories.UserRepository;
import com.naugroup3.interlude.services.UsernameService;
import com.naugroup3.interlude.utils.Validator;
import com.naugroup3.interlude.utils.jwt.JWTUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    public record LoginRequest(
        @NotBlank(message = "Email can't be blank") String email, 
        @NotBlank @Size(min = 8, message = "Password must be at least 8 charcters long") String password) {}
    
    public record SignupRequest(
        @NotBlank(message = "Username can't be blank") 
            @Pattern(regexp = Validator.USERNAME_REGEX, message = "Username must be 3-16 characters: letters, numbers, _ or -")
                String username,
        @Email(message = "Invalid email format")
            @NotBlank(message = "Email can't be blank")
                String email,
        @NotBlank(message = "Password can't be blank")
            @Size(min = 8, message = "Password must be at least 8 charcters long")
                String password) {}

    public record UsernameCheckResponse(boolean exists) {}

    private final UserRepository user_repository;
    private final UsernameService username_service;
    private final PasswordEncoder password_encoder;
    private final AuthenticationManager authentication_manager;
    private final JWTUtils jwt_utils;

    @GetMapping("/username-check")
    public UsernameCheckResponse username_check(@RequestParam String username) {
        if(!Validator.validate_username(username))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username");
        return new UsernameCheckResponse(username_service.exists(username));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        if(user_repository.existsByEmail(request.email()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already used");
        if(username_service.exists(request.username()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already used");
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email().toLowerCase());
        user.setPassword_hash(password_encoder.encode(request.password()));
        try {
            user_repository.save(user);
        } catch (DataIntegrityViolationException e) {
            // race condition
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email or username already used");
        }
        username_service.insert(user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(jwt_utils.generate_token(user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody LoginRequest request) {
        final UsernamePasswordAuthenticationToken auth_token = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        try {
            authentication_manager.authenticate(auth_token);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        final String token = jwt_utils.generate_token(request.email());
        return ResponseEntity.ok(token);
    }
}