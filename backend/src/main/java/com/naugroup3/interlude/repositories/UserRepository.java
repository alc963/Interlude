package com.naugroup3.interlude.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
