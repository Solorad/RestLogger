package com.morenkov.restlogger.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * @author emorenkov
 */
@Entity
public class Authentication {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "access_token")
    @Size(max = 32)
    private String accessToken;

    @Column(name = "authentication_time")
    private LocalDateTime authenticationTime;


    protected Authentication() {
    }

    public Authentication(Application application, String accessToken, LocalDateTime authenticationTime) {
        this.application = application;
        this.accessToken = accessToken;
        this.authenticationTime = authenticationTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getAuthenticationTime() {
        return authenticationTime;
    }

    public void setAuthenticationTime(LocalDateTime authenticationTime) {
        this.authenticationTime = authenticationTime;
    }
}
