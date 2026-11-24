package com.naugroup3.interlude.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.repositories.UserRepository;
import com.naugroup3.interlude.services.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {
    public record UserResponse(Long id, String username) {}

    private final UserRepository user_repository;
    private final CurrentUserService current_user_service;

    @GetMapping
    public List<UserResponse> list_users(@AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        return user_repository.findByIdNotOrderByUsernameAsc(user_id).stream()
            .map(UsersController::to_user_response).toList();
    }

    @GetMapping("/{id}")
    public UserResponse get_user(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        current_user_service.require_user_id(principal);
        final User user = user_repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return to_user_response(user);
    }

    private static UserResponse to_user_response(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
