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
    private static final Logger logger = LogManager.getLogger(AuthenticationService.class);

    private Integer accessPerMinute;
    private Integer waitOnBlockMinutes;

    // use here in-memory concurrent hash table instead of using
    private final ConcurrentHashMap<String, AccessDescription> appAccessRateMap;

    public ApplicationRateService() {
        this.appAccessRateMap = new ConcurrentHashMap<>();
    }

    public boolean checkApplicationAccessRateExceeded(String applicationId) {
        logger.debug("check {}", applicationId);
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
            if (lastUse.until(now, ChronoUnit.MINUTES) > waitOnBlockMinutes) {
                accessDescription.counter = 1;
            } else if (accessDescription.counter++ > accessPerMinute) {
                return true;
            }
        }
        return false;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AccessDescription)) {
                return false;
            }

            AccessDescription that = (AccessDescription) o;

            if (counter != that.counter) {
                return false;
            }
            return !(lastUse != null ? !lastUse.equals(that.lastUse) : that.lastUse != null);

        }

        @Override
        public int hashCode() {
            int result = counter;
            result = 31 * result + (lastUse != null ? lastUse.hashCode() : 0);
            return result;
        }
    }


    public Integer getAccessPerMinute() {
        return accessPerMinute;
    }

    public void setAccessPerMinute(Integer accessPerMinute) {
        this.accessPerMinute = accessPerMinute;
    }

    public Integer getWaitOnBlockMinutes() {
        return waitOnBlockMinutes;
    }

    public void setWaitOnBlockMinutes(Integer waitOnBlockMinutes) {
        this.waitOnBlockMinutes = waitOnBlockMinutes;
    }
}
