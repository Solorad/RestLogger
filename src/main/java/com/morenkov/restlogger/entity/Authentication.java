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

    @Column(name = "access_token_hash")
    @Size(max = 32)
    private String accessTokenHash;

    @Column(name = "authentication_time")
    private LocalDateTime authenticationTime;


    protected Authentication() {
    }

    public Authentication(Application application, String accessTokenHash, LocalDateTime authenticationTime) {
        this.application = application;
        this.accessTokenHash = accessTokenHash;
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

    public String getAccessTokenHash() {
        return accessTokenHash;
    }

    public void setAccessTokenHash(String accessTokenHash) {
        this.accessTokenHash = accessTokenHash;
    }

    public LocalDateTime getAuthenticationTime() {
        return authenticationTime;
    }

    public void setAuthenticationTime(LocalDateTime authenticationTime) {
        this.authenticationTime = authenticationTime;
    }
}
