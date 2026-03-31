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
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private String message;

    // Constructor for simple success messages
    public AuthResponse(String message) {
        this.message = message;
    }

    // Constructor for login/signup response
    public AuthResponse(String accessToken, String refreshToken, Long userId, 
                       String username, String email, String fullName, Set<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }
}