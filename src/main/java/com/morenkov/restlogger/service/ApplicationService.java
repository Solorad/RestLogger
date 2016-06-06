package com.morenkov.restlogger.service;

import com.morenkov.restlogger.dto.LogRequest;
import com.morenkov.restlogger.dto.LogResponse;
import com.morenkov.restlogger.entity.Application;
import com.morenkov.restlogger.entity.Authentication;
import com.morenkov.restlogger.entity.Log;
import com.morenkov.restlogger.repository.ApplicationRepository;
import com.morenkov.restlogger.repository.AuthenticationRepository;
import com.morenkov.restlogger.repository.LogRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author emorenkov
 */
@Service
public class ApplicationService {
    private static final Logger logger = LogManager.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationRateService applicationRateService;
    private final AuthenticationRepository authenticationRepository;
    private final AuthenticationService authenticationService;
    private final LogRepository logRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationRateService applicationRateService,
                              AuthenticationRepository authenticationRepository,
                              AuthenticationService authenticationService, LogRepository logRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationRateService = applicationRateService;
        this.authenticationRepository = authenticationRepository;
        this.authenticationService = authenticationService;
        this.logRepository = logRepository;
    }


    @Transactional
    public ResponseEntity<?> registerEndpoint(String displayName) {
        if (StringUtils.isEmpty(displayName) || displayName.length() > 32) {
            logger.warn("display name must be not null and less than 32 symbol.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // it is may be needed to show application secret to user in future on UI for some purpose.
        // that's why store in db it without hashing
        Application application = new Application(UUID.randomUUID().toString().replace("-", ""), displayName);
        application = applicationRepository.save(application);
        logger.info("new application was registered: '{}'", application);
        return new ResponseEntity<>(application, HttpStatus.OK);
    }

    @Async
    @Transactional
    public ListenableFuture<ResponseEntity<?>> writeLog(String accessToken, LogRequest logRequest) {
        if (!validateInput(logRequest, accessToken)) {
            logger.warn("Input params are not correct for logging.");
            return new AsyncResult<>(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        }
        //check rate is not exceeded
        if (applicationRateService.checkApplicationAccessRateExceeded(logRequest.getApplicationId())) {
            logger.warn("Access rate was exceeded for application.");
            return new AsyncResult<>(new ResponseEntity<>("rate limit exceeded", HttpStatus.FORBIDDEN));
        }

        List<Authentication> lastAppAuth =
                authenticationRepository.findLastAppAuth(logRequest.getApplicationId(), new PageRequest(0, 1));
        if (!validateAuthentication(accessToken, lastAppAuth)) {
            logger.warn("Provided access token is not equals to application token.");
            return new AsyncResult<>(new ResponseEntity<>(new LogResponse(false), HttpStatus.FORBIDDEN));
        }

        Authentication authentication = lastAppAuth.get(0);
        authentication.setAuthenticationTime(LocalDateTime.now());
        authenticationRepository.save(authentication); // update authentication use last time.

//        Authentication authentication = lastAppAuth.get(0);
        Log log = new Log(authentication.getApplication(), logRequest.getLogger(), logRequest.getLevel(),
                          logRequest.getMessage());
        logRepository.save(log);

        logger.info("log '{}' was persisted.", log);
        return new AsyncResult<>(new ResponseEntity<>(new LogResponse(true), HttpStatus.OK));
    }


    private boolean validateAuthentication(String accessToken, List<Authentication> lastAppAuth) {
        return lastAppAuth.size() == 1
               && lastAppAuth.get(0).getAuthenticationTime().until(LocalDateTime.now(), ChronoUnit.MINUTES)
                  <= authenticationService.getSessionLifeTimeMin()
               && accessToken.equals(lastAppAuth.get(0).getAccessToken());
    }

    private boolean validateInput(LogRequest logRequest, String accessToken) {
        return logRequest != null
               && !isEmpty(logRequest.getApplicationId())
               && !isEmpty(logRequest.getLevel())
               && !isEmpty(logRequest.getLogger())
               && !isEmpty(logRequest.getMessage())
               && !isEmpty(accessToken)
               && logRequest.getLevel().length() < 256
               && logRequest.getLogger().length() < 256
               && logRequest.getMessage().length() < 2048;
    }
}
