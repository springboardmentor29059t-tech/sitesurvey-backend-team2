package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * OTP Request Entity.
 *
 * FIX: replaced @Data with explicit @Getter + @Setter.
 *
 * @Data includes @EqualsAndHashCode and @ToString, both of which generate
 * methods that can conflict with the hand-written isExpired() / isValid()
 * helpers (the compiler sees isExpired as a potential getter for a field
 * "expired" and gets confused).  Using @Getter + @Setter avoids that and
 * keeps the class clean.
 *
 * The Boolean field "isUsed" will get:
 *   Lombok getter → getIsUsed()   ← matches AuthService call otpRequest.getIsUsed()
 *   Lombok setter → setIsUsed()   ← matches AuthService call otpRequest.setIsUsed(true)
 */
@Entity
@Table(name = "otp_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "is_used")
    private Boolean isUsed = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * These are business-logic helpers, not getters for a field.
     * Naming them is*() is fine because there is no field called
     * "expired" or "valid" — Lombok won't clash with them.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isValid() {
        return !isUsed && !isExpired();
    }
}