package com.morenkov.restlogger.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * @author emorenkov
 */
@Entity
public class Log {

    @Id
    @GeneratedValue
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "logger")
    @Size(max = 256)
    private String logger;

    @Column(name = "level")
    @Size(max = 256)
    private String level;

    @Column(name = "message")
    @Size(max = 2048)
    private String message;

    protected Log() {
    }

    public Log(Application application, String logger, String level, String message) {
        this.application = application;
        this.logger = logger;
        this.level = level;
        this.message = message;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
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


    @Override
    public String toString() {
        return "Log{" +
               "logId=" + logId +
               ", application=" + application +
               ", logger='" + logger + '\'' +
               ", level='" + level + '\'' +
               ", message='" + message + '\'' +
               '}';
    }
}
