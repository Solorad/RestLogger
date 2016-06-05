package com.morenkov.restlogger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author emorenkov
 */
public class LogRequest {
    @JsonProperty(value = "application_id")
    private String applicationId;
    private String logger;
    private String level;
    private String message;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
