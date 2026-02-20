package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * User Repository - Database operations for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if email exists
     */
    Boolean existsByEmail(String email);

    /**
     * Check if username exists
     */
    Boolean existsByUsername(String username);

    /**
     * Check if phone number exists
     */
    Boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find user by email or username (for login)
     */
    @Query("SELECT u FROM User u WHERE u.email = ?1 OR u.username = ?1")
    Optional<User> findByEmailOrUsername(String identifier);
}