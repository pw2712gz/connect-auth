package com.github.pw2712gz.connect_auth.repository;

import com.github.pw2712gz.connect_auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for accessing User entities by ID or email.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
