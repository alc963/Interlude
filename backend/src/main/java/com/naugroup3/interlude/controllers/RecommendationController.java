package com.naugroup3.interlude.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.RecommendedTrack;
import com.naugroup3.interlude.services.CurrentUserService;
import com.naugroup3.interlude.services.RecommendationService;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    private final RecommendationService recommendation_service;
    private final CurrentUserService current_user_service;

    public RecommendationController(RecommendationService recommendation_service, CurrentUserService current_user_service) {
        this.recommendation_service = recommendation_service;
        this.current_user_service = current_user_service;
    }

    @GetMapping
    public List<RecommendedTrack> getRecommendations(
        @RequestParam(defaultValue = "20") int limit,
        @AuthenticationPrincipal UserDetails principal
    ) {
        if(limit < 1 || limit > 50) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 50");

        return recommendation_service.recommend(current_user_service.require_user_id(principal), limit);
    }
}
