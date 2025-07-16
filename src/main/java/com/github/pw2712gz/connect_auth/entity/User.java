package com.github.pw2712gz.connect_auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents an OAuth2-authenticated user (GitHub, Google, etc.).
 * Stores profile information and login metadata.
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

    @Column(nullable = false)
    private String email;

    private String firstName;
    private String lastName;
    private String avatarUrl;

    /**
     * Whether the user is active in the system.
     * Currently always true after OAuth2 login.
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
