package com.morenkov.restlogger.rest;

import com.morenkov.restlogger.dto.LogRequest;
import com.morenkov.restlogger.dto.RegisterRequest;
import com.morenkov.restlogger.service.ApplicationService;
import com.morenkov.restlogger.service.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

/**
 * All requests were implemented asynchronously (not only log) to
 * make controller uniform.
 *
 * @author emorenkov
 */
@RestController
public class LogController {
    private static final Logger logger = LogManager.getLogger(LogController.class);

    private final AuthenticationService authenticationService;
    private final ApplicationService applicationService;

    @Autowired
    public LogController(AuthenticationService authenticationService, ApplicationService applicationService) {
        this.authenticationService = authenticationService;
        this.applicationService = applicationService;
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleBadInput(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("Invalid json");
    }




    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public ResponseEntity<?> authenticate(@RequestHeader("Authorization") String authorization) {
        logger.info("authorization started with  credentials '{}'", authorization);
        return authenticationService.authenticate(authorization);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("register new application started.");
        return applicationService.registerEndpoint(registerRequest.getDisplayName());
    }

    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ListenableFuture<ResponseEntity<?>> log(@RequestHeader("Authorization") String accessToken,
                                                   @RequestBody LogRequest logRequest) {
        logger.info("log message");
        return applicationService.writeLog(accessToken, logRequest);
    }
}
