package com.morenkov.restlogger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author emorenkov
 */
public class RegisterRequest {
    @JsonProperty(value="display_name")
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
