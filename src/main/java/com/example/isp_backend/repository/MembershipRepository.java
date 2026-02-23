package com.example.isp_backend.repository;

import com.example.isp_backend.entity.Membership;
import com.example.isp_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByUser(User user);
}
