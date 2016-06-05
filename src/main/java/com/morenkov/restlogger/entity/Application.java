package com.morenkov.restlogger.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

/**
 * @author emorenkov
 */
@Entity
public class Application {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "application_id")
    @Size(max = 32)
    private String applicationId;

    @Size(max = 32)
    private String secret;

    @Column(name = "display_name")
    @Size(max = 32)
    private String displayName;

    protected Application() {
    }

    public Application(String secret, String displayName) {
        this.secret = secret;
        this.displayName = displayName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        // don't add secret in toString
        return "Application{" +
               "applicationId='" + applicationId + '\'' +
               ", displayName='" + displayName + '\'' +
               '}';
    }
}
