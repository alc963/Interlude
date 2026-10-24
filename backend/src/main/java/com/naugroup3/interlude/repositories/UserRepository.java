package com.naugroup3.interlude.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.naugroup3.interlude.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("SELECT u.id FROM User u WHERE u.email = LOWER(:email)")
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u.username FROM User u")
    List<String> findAllUsernames();
}
