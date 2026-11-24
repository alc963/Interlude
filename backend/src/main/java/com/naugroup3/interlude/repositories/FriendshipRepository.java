package com.naugroup3.interlude.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.naugroup3.interlude.models.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE (f.user_requested.id = :user_id_a AND f.user_recipient.id = :user_id_b) OR (f.user_requested.id = :user_id_b AND f.user_recipient.id = :user_id_a)")
    Optional<Friendship> findBetween(@Param("user_id_a") Long user_id_a, @Param("user_id_b") Long user_id_b);

    @Query("SELECT f FROM Friendship f WHERE f.status = :status AND (f.user_requested.id = :user_id OR f.user_recipient.id = :user_id)")
    List<Friendship> findAllForUser(@Param("user_id") Long user_id, @Param("status") Friendship.Status status);
}
