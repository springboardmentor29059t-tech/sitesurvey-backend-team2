package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.email = ?1 OR u.username = ?1")
    Optional<User> findByEmailOrUsername(String identifier);

    /**
     * ✅ THIS IS THE MISSING METHOD:
     * Finds all clients assigned to a specific engineer
     */
    List<User> findByAssignedEngineer(User engineer);

    /**
     * Find all users with the CLIENT role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_CLIENT' OR r.name = 'CLIENT'")
    List<User> findAllClients();

    /**
     * Find all users with the ONSITE_ENGINEER role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ONSITE_ENGINEER' OR r.name = 'ONSITE_ENGINEER'")
    List<User> findAllEngineers();
}