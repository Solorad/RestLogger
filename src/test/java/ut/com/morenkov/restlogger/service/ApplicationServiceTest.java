package ut.com.morenkov.restlogger.service;

import com.morenkov.restlogger.entity.Application;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

/**
 * @author emorenkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceTest {
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApplicationRateService applicationRateService;
    @Mock
    private AuthenticationRepository authenticationRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private LogRepository logRepository;

    private ApplicationService applicationService;

    @Before
    public void setUp() throws Exception {
        applicationService =
                new ApplicationService(applicationRepository, applicationRateService, authenticationRepository,
                                       authenticationService, logRepository, new BCryptPasswordEncoder());
    }


    @Test
    public void registerEndpointNull() throws ExecutionException, InterruptedException {
        ListenableFuture<ResponseEntity<?>> responseEntityListenableFuture = applicationService.registerEndpoint(null);
        ResponseEntity<?> responseEntity = responseEntityListenableFuture.get();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void registerEndpoint() throws ExecutionException, InterruptedException {
        ListenableFuture<ResponseEntity<?>> responseEntityListenableFuture = applicationService.registerEndpoint("test");
        ResponseEntity responseEntity = responseEntityListenableFuture.get();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Application body = (Application) responseEntity.getBody();
        assertNotNull(body);
        assertNotNull(body.getApplicationId());
        assertNotNull(body.getSecretHash());
        assertEquals("test", body.getDisplayName());
    }
}