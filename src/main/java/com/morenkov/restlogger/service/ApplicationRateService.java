package com.morenkov.restlogger.service;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author emorenkov
 */
@Service
@ConfigurationProperties(prefix = "rest.logger.request.rate-limiting")
public class ApplicationRateService {
    private static final Logger logger = LogManager.getLogger(ApplicationRateService.class);
    // use here in-memory concurrent hash table instead of using
    private final ConcurrentHashMap<String, AccessDescription> appAccessRateMap;
    private Integer accessPerMin;
    private Integer waitOnBlockMin;

    public ApplicationRateService() {
        this.appAccessRateMap = new ConcurrentHashMap<>();
    }

    public boolean checkApplicationAccessRateExceeded(String applicationId) {
        logger.info("check {}", applicationId);
        AccessDescription accessDescription = appAccessRateMap.get(applicationId);
        if (accessDescription != null) {
            if (checkExistedAccess(accessDescription)) {
                return true;
            }
        } else {
            accessDescription = new AccessDescription(LocalDateTime.now());
            AccessDescription result = appAccessRateMap.putIfAbsent(applicationId, accessDescription);
            if (result != null && !result.equals(accessDescription)) { // another thread added description before.
                return checkExistedAccess(result);
            }
        }

        return false;
    }

    private boolean checkExistedAccess(AccessDescription accessDescription) {
        synchronized (accessDescription) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastUse = accessDescription.lastUse;
            accessDescription.lastUse = now;
            if (lastUse.until(now, ChronoUnit.MINUTES) >= waitOnBlockMin) {
                accessDescription.counter = 1;
            } else if (accessDescription.counter++ >= accessPerMin) {
                return true;
            }
        }
        return false;
    }

    public Integer getAccessPerMin() {
        return accessPerMin;
    }

    public void setAccessPerMin(Integer accessPerMin) {
        this.accessPerMin = accessPerMin;
    }

    public Integer getWaitOnBlockMin() {
        return waitOnBlockMin;
    }

    public void setWaitOnBlockMin(Integer waitOnBlockMin) {
        this.waitOnBlockMin = waitOnBlockMin;
    }

    private class AccessDescription {
        // access to counter would be only in synchronized on instance block
        // no need for AtomicInt
        public int counter;
        public LocalDateTime lastUse;


        public AccessDescription(LocalDateTime lastUse) {
            this.lastUse = lastUse;
            this.counter = 1;
        }
    }
}
