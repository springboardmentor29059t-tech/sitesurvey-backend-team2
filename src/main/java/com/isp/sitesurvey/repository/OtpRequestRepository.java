package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OTP Request Repository - Database operations for OTP requests
 */
@Repository
public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {

    /**
     * Find the latest valid OTP for an email
     */
    @Query("SELECT o FROM OtpRequest o WHERE o.email = ?1 AND o.isUsed = false " +
           "AND o.expiryTime > ?2 ORDER BY o.createdAt DESC")
    Optional<OtpRequest> findLatestValidOtp(String email, LocalDateTime currentTime);

    /**
     * Find OTP by email and code
     */
    Optional<OtpRequest> findByEmailAndOtpCode(String email, String otpCode);

    /**
     * Delete expired OTPs (cleanup)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpRequest o WHERE o.expiryTime < ?1")
    void deleteExpiredOtps(LocalDateTime currentTime);

    /**
     * Invalidate all previous OTPs for an email
     */
    @Modifying
    @Transactional
    @Query("UPDATE OtpRequest o SET o.isUsed = true WHERE o.email = ?1 AND o.isUsed = false")
    void invalidatePreviousOtps(String email);
}