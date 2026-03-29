package com.isp.sitesurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

/**
 * Authentication Response DTO
 * Returned after successful login or signup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private final String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private String message;

    // 👇 ADDED: The missing phone number field
    private String phoneNumber;

    // Constructor for simple success messages
    public AuthResponse(String message) {
        this.message = message;
    }

    // 👇 ADDED: phoneNumber parameter to constructor
    public AuthResponse(String accessToken, String refreshToken, Long userId, 
                       String username, String email, String fullName, Set<String> roles, String phoneNumber) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.phoneNumber = phoneNumber; 
    }
}