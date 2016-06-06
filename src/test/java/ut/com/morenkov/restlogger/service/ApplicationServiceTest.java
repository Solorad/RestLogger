package ut.com.morenkov.restlogger.service;

import com.morenkov.restlogger.dto.LogRequest;
import com.morenkov.restlogger.entity.Application;
import com.morenkov.restlogger.entity.Authentication;
import com.morenkov.restlogger.repository.ApplicationRepository;
import com.morenkov.restlogger.repository.AuthenticationRepository;
import com.morenkov.restlogger.repository.LogRepository;
import com.morenkov.restlogger.service.ApplicationRateService;
import com.morenkov.restlogger.service.ApplicationService;
import com.morenkov.restlogger.service.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author emorenkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceTest {
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AuthenticationRepository authenticationRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private LogRepository logRepository;

    private ApplicationService applicationService;
    private LogRequest logRequest;
    private Application application;

    @Before
    public void setUp() throws Exception {
        ApplicationRateService applicationRateService = new ApplicationRateService();
        applicationRateService.setAccessPerMin(60);
        applicationRateService.setWaitOnBlockMin(5);
        applicationService = new ApplicationService(applicationRepository, applicationRateService, authenticationRepository,
                                                    authenticationService, logRepository);

        logRequest = new LogRequest();
        logRequest.setApplicationId("application_id");
        logRequest.setLevel("DEBUG");
        logRequest.setLogger("com.package.Logger");
        logRequest.setMessage("message");

        List<Authentication> authentications = new ArrayList<>();
        application = new Application("secret", "test");
        application.setApplicationId("application_id");
        authentications.add(new Authentication(application, "access_token", LocalDateTime.now()));
        when(authenticationRepository.findLastAppAuth(eq("application_id"), any(PageRequest.class)))
                .thenReturn(authentications);
        when(authenticationService.getSessionLifeTimeMin()).thenReturn(30);
    }


    @Test
    public void registerEndpointNull() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = applicationService.registerEndpoint(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void registerEndpoint() throws ExecutionException, InterruptedException {
        when(applicationRepository.save(any(Application.class))).thenReturn(application);
        ResponseEntity responseEntity = applicationService.registerEndpoint("test");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Application body = (Application) responseEntity.getBody();
        assertNotNull(body);
        assertNotNull(body.getApplicationId());
        assertNotNull(body.getSecret());
        assertEquals("test", body.getDisplayName());
    }

    @Test
    public void writeLogsNullInput() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = applicationService.writeLog(null, null).get();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void writeLogsEmpty() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = applicationService.writeLog("", new LogRequest()).get();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void writeLogsNoAuthenticationForApplication() throws ExecutionException, InterruptedException {
        when(authenticationRepository.findLastAppAuth(eq("application_id"), any(PageRequest.class)))
                .thenReturn(Collections.EMPTY_LIST);
        ResponseEntity<?> responseEntity = applicationService.writeLog("access_token", logRequest).get();
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    public void writeLogsInvalidAccessToken() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = applicationService.writeLog("invalid_access_token", logRequest).get();
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    public void writeLogs() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = applicationService.writeLog("access_token", logRequest).get();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }



    @Test
    public void writeLogsRateExceeded() throws ExecutionException, InterruptedException {
        for (int i = 0; i < 60; i++) {
            applicationService.writeLog("access_token", logRequest);
        }
        ResponseEntity<?> responseEntity = applicationService.writeLog("access_token", logRequest).get();
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }
}