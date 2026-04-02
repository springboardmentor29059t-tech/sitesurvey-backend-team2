package com.survey.site.dto;

public class LoginResponse {

    private String token;
    private String role;

    public LoginResponse() {}

    public LoginResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }
}