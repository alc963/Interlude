package com.naugroup3.interlude.services;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.naugroup3.interlude.repositories.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsernameService {
    private static final double FALSE_POSITIVE_RATE = 0.0001;
    private static final int MIN_EXPECTED_INSERTIONS = 10_000;

    private final UserRepository user_repository;
    private BloomFilter<String> bloom_filter;

    @PostConstruct
    void load_usernames() {
        final List<String> usernames = user_repository.findAllUsernames();
        final int expected = Math.max(MIN_EXPECTED_INSERTIONS, usernames.size() * 2);
        bloom_filter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), expected, FALSE_POSITIVE_RATE);
        usernames.forEach(bloom_filter::put);
    }

    public boolean exists(String username) {
        if(!bloom_filter.mightContain(username)) return false;
        return user_repository.existsByUsername(username);
    }

    public void insert(String username) {
        bloom_filter.put(username);
    }
}
