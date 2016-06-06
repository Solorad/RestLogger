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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public static final int DEFAULT_SESSION_LIFETIME_MIN = 30;
    public static final String SESSION_LIFETIME_PROPERTY = "session_lifetime_min";


    private final AuthenticationRepository authenticationRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationPropertyRepository applicationPropertyRepository;


    @Autowired
    public AuthenticationService(AuthenticationRepository authenticationRepository,
                                 ApplicationRepository applicationRepository,
                                 ApplicationPropertyRepository applicationPropertyRepository) {
        this.authenticationRepository = authenticationRepository;
        this.applicationRepository = applicationRepository;
        this.applicationPropertyRepository = applicationPropertyRepository;
    }

    public ResponseEntity<?> authenticate(String authorization) {
        logger.info("authenticate method started.");
        if (authorization == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        String[] authArray = authorization.split(":");
        if (authArray.length != 2) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return getAuthenticationResponse(authArray[0], authArray[1]);
    }

    @Transactional
    private ResponseEntity<?> getAuthenticationResponse(String applicationId,
                                                                          String applicicationSecret) {
        List<Authentication> authList = authenticationRepository.findLastAppAuth(applicationId, new PageRequest(0, 1));
        if (authList.size() == 1 && authList.get(0).getAuthenticationTime()
                                            .until(LocalDateTime.now(), ChronoUnit.MINUTES) < getSessionLifeTimeMin()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Application application = applicationRepository.findOne(applicationId);

        if (application == null || !applicicationSecret.equals(application.getSecret())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String accessToken = UUID.randomUUID().toString().replace("-", "");
        Authentication authentication = new Authentication(application, accessToken, LocalDateTime.now());
        authenticationRepository.save(authentication);

        return new ResponseEntity<>(new AuthResponse(accessToken), HttpStatus.OK);
    }


    public int getSessionLifeTimeMin() {
        try {
            ApplicationProperty lifeTimeMinProperty = applicationPropertyRepository.findOne(SESSION_LIFETIME_PROPERTY);
            return lifeTimeMinProperty == null ? DEFAULT_SESSION_LIFETIME_MIN :
                   Integer.valueOf(lifeTimeMinProperty.getPropertyValue());
        } catch (NumberFormatException e) { // if in db not valid integer
            return DEFAULT_SESSION_LIFETIME_MIN;
        }
    }
}
