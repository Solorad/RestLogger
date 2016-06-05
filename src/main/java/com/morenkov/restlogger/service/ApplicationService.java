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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationRateService applicationRateService,
                              AuthenticationRepository authenticationRepository,
                              AuthenticationService authenticationService, LogRepository logRepository,
                              BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.applicationRepository = applicationRepository;
        this.applicationRateService = applicationRateService;
        this.authenticationRepository = authenticationRepository;
        this.authenticationService = authenticationService;
        this.logRepository = logRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Async
    @Transactional
    public ListenableFuture<ResponseEntity<?>> registerEndpoint(String displayName) {
        if (displayName == null) {
            return new AsyncResult<>(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        }
        // it is may be needed to show application secret to user in future on UI for some purpose.
        // that's why store in db it without hashing
        Application application = new Application(UUID.randomUUID().toString().replace("-", ""), displayName);
        application = applicationRepository.save(application);
        ResponseEntity<Application> response = new ResponseEntity<>(application, HttpStatus.OK);
        return new AsyncResult<>(response);
    }

    @Async
    @Transactional
    public ListenableFuture<ResponseEntity<?>> writeLog(String accessToken, LogRequest logRequest) {
        if (!validateInput(logRequest, accessToken)) {
            return new AsyncResult<>(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        }
        //check rate is not exceeded
        if (applicationRateService.checkApplicationAccessRateExceeded(logRequest.getApplicationId())) {
            return new AsyncResult<>(new ResponseEntity<>("rate limit exceeded", HttpStatus.FORBIDDEN));
        }

        List<Authentication> lastAppAuth =
                authenticationRepository.findLastAppAuth(logRequest.getApplicationId(), new PageRequest(0, 1));
        if (!validateAuthentication(accessToken, lastAppAuth)) {
            return new AsyncResult<>(new ResponseEntity<>(new LogResponse(false), HttpStatus.FORBIDDEN));
        }

        Authentication authentication = lastAppAuth.get(0);
        authentication.setAuthenticationTime(LocalDateTime.now());
        authenticationRepository.save(authentication); // update authentication use last time.

//        Authentication authentication = lastAppAuth.get(0);
        Log log = new Log(authentication.getApplication(), logRequest.getLogger(), logRequest.getLevel(),
                          logRequest.getMessage());
        logRepository.save(log);
        return new AsyncResult<>(new ResponseEntity<>(new LogResponse(true), HttpStatus.OK));
    }


    private boolean validateAuthentication(String accessToken, List<Authentication> lastAppAuth) {
        return lastAppAuth.size() == 1
               && lastAppAuth.get(0).getAuthenticationTime().until(LocalDateTime.now(), ChronoUnit.MINUTES)
                  <= authenticationService.getSessionLifeTimeMin()
               || !bCryptPasswordEncoder.matches(lastAppAuth.get(0).getAccessTokenHash(), accessToken);
    }

    private boolean validateInput(LogRequest logRequest, String accessToken) {
        return logRequest != null
               && !isEmpty(logRequest.getApplicationId())
               && !isEmpty(logRequest.getLevel())
               && !isEmpty(logRequest.getLogger())
               && !isEmpty(logRequest.getMessage())
               && !isEmpty(accessToken);
    }
}
