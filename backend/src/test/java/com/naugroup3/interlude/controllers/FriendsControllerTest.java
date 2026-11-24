package com.naugroup3.interlude.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.Friendship;
import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.repositories.FriendshipRepository;
import com.naugroup3.interlude.repositories.UserRepository;
import com.naugroup3.interlude.services.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class FriendsControllerTest {
    @Mock private FriendshipRepository friendship_repository;
    @Mock private UserRepository user_repository;
    @Mock private CurrentUserService current_user_service;
    @Mock private UserDetails principal;
    @InjectMocks private FriendsController controller;

    private final User user_me = user(1L, "user_me");
    private final User user_them = user(2L, "user_them");

    @BeforeEach
    void setUp() {
        when(current_user_service.require_user_id(principal)).thenReturn(1L);
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Friendship between(Friendship friendship) {
        when(friendship_repository.findBetween(1L, 2L)).thenReturn(Optional.ofNullable(friendship));
        return friendship;
    }

    private static void assert_status(Runnable call, HttpStatus status) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ResponseStatusException.class, ex -> assertThat(ex.getStatusCode()).isEqualTo(status));
    }

    @Test
    void send_request_creates_pending() {
        when(user_repository.findById(2L)).thenReturn(Optional.of(user_them));
        between(null);
        when(friendship_repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(controller.send_request(2L, principal).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void send_request_to_self_fails() {
        assert_status(() -> controller.send_request(1L, principal), HttpStatus.BAD_REQUEST);
    }

    @Test
    void send_request_twice_conflicts() {
        when(user_repository.findById(2L)).thenReturn(Optional.of(user_them));
        between(new Friendship(user_me, user_them));
        assert_status(() -> controller.send_request(2L, principal), HttpStatus.CONFLICT);
    }

    @Test
    void accept_request_accepts_incoming() {
        Friendship request = between(new Friendship(user_them, user_me));
        controller.accept_request(2L, principal);
        assertThat(request.getStatus()).isEqualTo(Friendship.Status.ACCEPTED);
    }

    @Test
    void accept_request_rejects_own_request() {
        between(new Friendship(user_me, user_them));
        assert_status(() -> controller.accept_request(2L, principal), HttpStatus.NOT_FOUND);
    }

    @Test
    void decline_request_deletes_incoming() {
        Friendship request = between(new Friendship(user_them, user_me));
        controller.decline_request(2L, principal);
        verify(friendship_repository).delete(request);
    }

    @Test
    void cancel_request_deletes_outgoing() {
        Friendship request = between(new Friendship(user_me, user_them));
        controller.cancel_request(2L, principal);
        verify(friendship_repository).delete(request);
    }

    @Test
    void remove_friend_rejects_pending() {
        between(new Friendship(user_me, user_them));
        assert_status(() -> controller.remove_friend(2L, principal), HttpStatus.NOT_FOUND);
    }

    @Test
    void list_requests_splits_incoming_and_outgoing() {
        User other = user(3L, "user_other");
        when(friendship_repository.findAllForUser(1L, Friendship.Status.PENDING)).thenReturn(List.of(new Friendship(user_them, user_me), new Friendship(user_me, other)));
        var response = controller.list_requests(principal);
        assertThat(response.incoming()).extracting(r -> r.username()).containsExactly("user_them");
        assertThat(response.outgoing()).extracting(r -> r.username()).containsExactly("user_other");
    }
}
