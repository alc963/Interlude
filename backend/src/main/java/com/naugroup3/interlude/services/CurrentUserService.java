package com.naugroup3.interlude.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.repositories.UserRepository;
import com.naugroup3.interlude.utils.jwt.JWTUtils;

@Service
public class CurrentUserService {
    private final UserRepository user_repository;
    private final JWTUtils jwt_utils;

    public CurrentUserService(UserRepository user_repository, JWTUtils jwt_utils) {
        this.user_repository = user_repository;
        this.jwt_utils = jwt_utils;
    }

    public Long require_user_id(UserDetails principal) {
        if (principal == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        return user_id_for_email(principal.getUsername());
    }

    public Long require_user_id(String jwt) {
        if (jwt == null || jwt.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        return user_id_for_email(jwt_utils.extract_email(jwt));
    }

    private Long user_id_for_email(String email) {
        return user_repository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
    }
}
