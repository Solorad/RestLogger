package com.morenkov.restlogger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author emorenkov
 */
public class AuthResponse {
    @JsonProperty(value="access_token")
    private String accessToken;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
