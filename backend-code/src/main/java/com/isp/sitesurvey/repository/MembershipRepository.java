package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByOrganizationId(Long organizationId);
    List<Membership> findByUserId(Long userId);
    Optional<Membership> findByOrganizationIdAndUserId(Long organizationId, Long userId);
}