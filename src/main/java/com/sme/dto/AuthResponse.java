package com.sme.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    // No-args constructor
    public AuthResponse() {
    }

//    // All-args constructor
//    public AuthResponse(String accessToken, String refreshToken) {
//        this.accessToken = accessToken;
//        this.refreshToken = refreshToken;
//    }

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters and setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
