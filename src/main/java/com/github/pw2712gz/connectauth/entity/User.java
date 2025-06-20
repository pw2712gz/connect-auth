package com.github.pw2712gz.connectauth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a user authenticated via OAuth2 (GitHub, Google, etc.).
 * Stores profile info and login metadata.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String avatarUrl;

    /**
     * Whether the user is active in the system.
     * Currently always true after login.
     */
    private boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant lastLogin;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.lastLogin = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
