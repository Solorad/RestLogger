package com.morenkov.restlogger.dto;

/**
 * @author emorenkov
 */
public class LogResponse {
    private Boolean success;

    public LogResponse(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
