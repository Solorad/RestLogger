package com.morenkov.restlogger.service;

import com.morenkov.restlogger.dto.AuthResponse;
import com.morenkov.restlogger.entity.Application;
import com.morenkov.restlogger.entity.ApplicationProperty;
import com.morenkov.restlogger.entity.Authentication;
import com.morenkov.restlogger.repository.ApplicationPropertyRepository;
import com.morenkov.restlogger.repository.ApplicationRepository;
import com.morenkov.restlogger.repository.AuthenticationRepository;
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

/**
 * @author emorenkov
 */
@Service
public class AuthenticationService {
    private static final Logger logger = LogManager.getLogger(AuthenticationService.class);

    private static final int DEFAULT_SESSION_LIFETIME_MIN = 30;
    private static final String SESSION_LIFETIME_PROPERTY = "session_lifetime_min";


    private final AuthenticationRepository authenticationRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationPropertyRepository applicationPropertyRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Autowired
    public AuthenticationService(AuthenticationRepository authenticationRepository,
                                 ApplicationRepository applicationRepository,
                                 ApplicationPropertyRepository applicationPropertyRepository,
                                 BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.authenticationRepository = authenticationRepository;
        this.applicationRepository = applicationRepository;
        this.applicationPropertyRepository = applicationPropertyRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Async
    public ListenableFuture<ResponseEntity<?>> authenticate(String authorization) {
        logger.debug("authenticate method started.");
        if (authorization == null) {
            return new AsyncResult<>(new ResponseEntity(HttpStatus.BAD_REQUEST));
        }
        String[] authArray = authorization.split(":");
        if (authArray.length != 2) {
            return new AsyncResult<>(new ResponseEntity(HttpStatus.BAD_REQUEST));
        }
        return getAuthenticationResponse(authArray[0], authArray[1]);
    }

    @Transactional
    private ListenableFuture<ResponseEntity<?>> getAuthenticationResponse(String applicationId,
                                                                          String applicicationSecret) {
        List<Authentication> authList = authenticationRepository.findLastAppAuth(applicationId, new PageRequest(0, 1));
        if (authList.size() == 1 && authList.get(0).getAuthenticationTime()
                                            .until(LocalDateTime.now(), ChronoUnit.MINUTES) < getSessionLifeTimeMin()) {
            return new AsyncResult<>(new ResponseEntity(HttpStatus.BAD_REQUEST));
        }

        Application application = applicationRepository.findOne(applicationId);

        if (application == null || !applicicationSecret.equals(application.getSecret())) {
            return new AsyncResult<>(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
        }

        String accessTokenHash = bCryptPasswordEncoder.encode(UUID.randomUUID().toString());
        Authentication authentication = new Authentication(application, accessTokenHash, LocalDateTime.now());
        authenticationRepository.save(authentication);

        ResponseEntity<AuthResponse> response = new ResponseEntity<>(new AuthResponse(accessTokenHash), HttpStatus.OK);
        return new AsyncResult<>(response);
    }


    public int getSessionLifeTimeMin() {
        ApplicationProperty lifeTimeMinProperty = applicationPropertyRepository.findOne(SESSION_LIFETIME_PROPERTY);
        return lifeTimeMinProperty == null ? DEFAULT_SESSION_LIFETIME_MIN :
               Integer.valueOf(lifeTimeMinProperty.getPropertyValue());
    }
}
