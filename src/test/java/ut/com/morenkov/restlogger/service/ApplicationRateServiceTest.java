package ut.com.morenkov.restlogger.service;

import com.morenkov.restlogger.service.ApplicationRateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author emorenkov
 */
public class ApplicationRateServiceTest {

    public static final String APPLICATION_ID = "testApplication";
    private ApplicationRateService applicationRateService;

    @Before
    public void setUp() throws Exception {
        applicationRateService = new ApplicationRateService();
        applicationRateService.setAccessPerMin(60);
        applicationRateService.setWaitOnBlockMin(5);
    }

    @Test
    public void testCheckApplicationAccessRateNotExceeded() {
        assertFalse(applicationRateService.checkApplicationAccessRateExceeded(APPLICATION_ID));
    }

    @Test
    public void testCheckApplicationAccessRateExceeded() {
        for (int i = 0; i < 60; i++) {
            applicationRateService.checkApplicationAccessRateExceeded(APPLICATION_ID);
        }
        assertTrue(applicationRateService.checkApplicationAccessRateExceeded(APPLICATION_ID));
    }
}