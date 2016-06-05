package ut.com.morenkov.restlogger.service;

import com.morenkov.restlogger.dto.AuthResponse;
import com.morenkov.restlogger.entity.Application;
import com.morenkov.restlogger.entity.ApplicationProperty;
import com.morenkov.restlogger.repository.ApplicationPropertyRepository;
import com.morenkov.restlogger.repository.ApplicationRepository;
import com.morenkov.restlogger.repository.AuthenticationRepository;
import com.morenkov.restlogger.service.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

import static com.morenkov.restlogger.service.AuthenticationService.DEFAULT_SESSION_LIFETIME_MIN;
import static com.morenkov.restlogger.service.AuthenticationService.SESSION_LIFETIME_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @author emorenkov
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    @Mock
    private AuthenticationRepository authenticationRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApplicationPropertyRepository applicationPropertyRepository;

    private AuthenticationService authenticationService;

    @Before
    public void setUp() throws Exception {
        authenticationService = new AuthenticationService(authenticationRepository, applicationRepository,
                                                          applicationPropertyRepository);

        Application application = new Application("app_secret", "application_name");
        application.setApplicationId("app_id");
        when(applicationRepository.findOne("app_id")).thenReturn(application);

    }

    @Test
    public void testGetSessionLifeTimeMinNoProperties() {
        int sessionLifeTimeMin = authenticationService.getSessionLifeTimeMin();
        assertEquals(DEFAULT_SESSION_LIFETIME_MIN, sessionLifeTimeMin);
    }

    @Test
    public void testGetSessionLifeTimeMinWithProperties() {
        ApplicationProperty applicationProperty = new ApplicationProperty(SESSION_LIFETIME_PROPERTY, "50");
        when(applicationPropertyRepository.findOne(SESSION_LIFETIME_PROPERTY)).thenReturn(applicationProperty);
        int sessionLifeTimeMin = authenticationService.getSessionLifeTimeMin();
        assertEquals(50, sessionLifeTimeMin);
    }

    @Test
    public void testGetSessionLifeTimeMinWithInvalidProperties() {
        ApplicationProperty applicationProperty = new ApplicationProperty(SESSION_LIFETIME_PROPERTY, "asdew");
        when(applicationPropertyRepository.findOne(SESSION_LIFETIME_PROPERTY)).thenReturn(applicationProperty);
        int sessionLifeTimeMin = authenticationService.getSessionLifeTimeMin();
        assertEquals(DEFAULT_SESSION_LIFETIME_MIN, sessionLifeTimeMin);
    }


    @Test
    public void testAuthenticateNull() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = authenticationService.authenticate(null).get();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testAuthenticateInvalidAuthentication() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = authenticationService.authenticate("just_simple_text").get();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testAuthenticateApplicationNotFound() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = authenticationService.authenticate("some_app_id:some__secret").get();
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testAuthenticateApplicationSecretNotEqual() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = authenticationService.authenticate("app_id:invalid_secret").get();
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testAuthenticateApplicationSecretEqual() throws ExecutionException, InterruptedException {
        ResponseEntity<?> responseEntity = authenticationService.authenticate("app_id:app_secret").get();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        AuthResponse response = (AuthResponse) responseEntity.getBody();
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
    }
}