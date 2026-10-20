package com.naugroup3.interlude.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 200)
    private String username;
    @Column(name = "email", length = 200)
    private String email;
    @Column(name = "password_hash", length = 256)
    private String password_hash;

    @Column(name = "spotify_token", length = 512)
    private String spotify_token;
}
