package ut.com.morenkov.restlogger.service;

import com.morenkov.restlogger.service.ApplicationRateService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * @author emorenkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationRateServiceTest {

    private ApplicationRateService applicationRateService;

    @Before
    public void setUp() throws Exception {
        applicationRateService = new ApplicationRateService();
    }
}