package com.naugroup3.interlude.controllers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.Friendship;
import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.repositories.FriendshipRepository;
import com.naugroup3.interlude.repositories.UserRepository;
import com.naugroup3.interlude.services.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendsController {
    public record FriendResponse(Long id, String username, Instant since) {}
    public record FriendRequestResponse(Long id, String username, Friendship.Status status) {}
    public record PendingRequest(Long id, String username, Instant sent_at) {}
    public record PendingRequestsResponse(List<PendingRequest> incoming, List<PendingRequest> outgoing) {}

    private final FriendshipRepository friendship_repository;
    private final UserRepository user_repository;
    private final CurrentUserService current_user_service;

    @PostMapping("/request/{id}")
    @Transactional
    public ResponseEntity<FriendRequestResponse> send_request(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        if (user_id.equals(id)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't friend yourself");

        final User user_recipient = user_repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        final var existing = friendship_repository.findBetween(user_id, id);
        if (existing.isPresent()) {
            final Friendship friendship = existing.get();
            if (friendship.getStatus() == Friendship.Status.ACCEPTED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Already friends");
            if (friendship.getUser_requested().getId().equals(user_id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request already sent");
            friendship.accept_friendship();
            return ResponseEntity.ok(new FriendRequestResponse(id, user_recipient.getUsername(), friendship.getStatus()));
        }

        final User user_requested = user_repository.getReferenceById(user_id);
        final Friendship friendship = friendship_repository.save(new Friendship(user_requested, user_recipient));
        return ResponseEntity.status(HttpStatus.CREATED).body(new FriendRequestResponse(id, user_recipient.getUsername(), friendship.getStatus()));
    }

    @PostMapping("/request/accept/{id}")
    @Transactional
    public FriendResponse accept_request(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        final Friendship friendship = require_incoming_request(user_id, id);
        friendship.accept_friendship();
        return to_friend_response(friendship, user_id);
    }

    @PostMapping("/request/decline/{id}")
    @Transactional
    public ResponseEntity<Void> decline_request(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        friendship_repository.delete(require_incoming_request(user_id, id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<FriendResponse> list_friends(@AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        return friendship_repository.findAllForUser(user_id, Friendship.Status.ACCEPTED).stream()
            .map(f -> to_friend_response(f, user_id)).toList();
    }

    @GetMapping("/requests")
    @Transactional(readOnly = true)
    public PendingRequestsResponse list_requests(@AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        final List<PendingRequest> incoming = new ArrayList<>();
        final List<PendingRequest> outgoing = new ArrayList<>();
        for (Friendship request : friendship_repository.findAllForUser(user_id, Friendship.Status.PENDING)) {
            final User other_user = request.other_user(user_id);
            final PendingRequest pending = new PendingRequest(other_user.getId(), other_user.getUsername(), request.getCreated_at());
            if (request.getUser_recipient().getId().equals(user_id)) incoming.add(pending);
            else outgoing.add(pending);
        }
        return new PendingRequestsResponse(incoming, outgoing);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public FriendResponse get_friend(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        return to_friend_response(require_friendship(user_id, id), user_id);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> remove_friend(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        friendship_repository.delete(require_friendship(user_id, id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/request/cancel/{id}")
    @Transactional
    public ResponseEntity<Void> cancel_request(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        final Long user_id = current_user_service.require_user_id(principal);
        final Friendship friendship = friendship_repository.findBetween(user_id, id)
            .filter(ship -> ship.getStatus() == Friendship.Status.PENDING)
            .filter(ship -> ship.getUser_requested().getId().equals(user_id))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending friend request to this user"));
        friendship_repository.delete(friendship);
        return ResponseEntity.noContent().build();
    }

    private Friendship require_incoming_request(Long user_id, Long user_requested_id) {
        return friendship_repository.findBetween(user_id, user_requested_id)
            .filter(friendship -> friendship.getStatus() == Friendship.Status.PENDING)
            .filter(friendship -> friendship.getUser_recipient().getId().equals(user_id))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending friend request from this user"));
    }

    private Friendship require_friendship(Long user_id, Long friend_id) {
        return friendship_repository.findBetween(user_id, friend_id)
            .filter(friendship -> friendship.getStatus() == Friendship.Status.ACCEPTED)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not friends with this user"));
    }

    private static FriendResponse to_friend_response(Friendship friendship, Long user_id) {
        final User friend = friendship.other_user(user_id);
        return new FriendResponse(friend.getId(), friend.getUsername(), friendship.getAccepted_at());
    }
}
