package com.naugroup3.interlude.models;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "friendships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {
    public enum Status { PENDING, ACCEPTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_requested_id", nullable = false)
    private User user_requested;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_recipient_id", nullable = false)
    private User user_recipient;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant created_at;
    @Column
    private Instant accepted_at;

    public Friendship(User user_requested, User user_recipient) {
        this.user_requested = user_requested;
        this.user_recipient = user_recipient;
        this.status = Status.PENDING;
    }

    public void accept_friendship() {
        this.status = Status.ACCEPTED;
        this.accepted_at = Instant.now();
    }

    public User other_user(Long user_id) {
        return user_requested.getId().equals(user_id) ? user_recipient : user_requested;
    }
}
