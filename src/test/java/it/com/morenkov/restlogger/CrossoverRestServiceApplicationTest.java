package it.com.morenkov.restlogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.morenkov.restlogger.CrossoverRestServiceApplication;
import com.morenkov.restlogger.dto.AuthResponse;
import com.morenkov.restlogger.entity.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dependant groups would be nice here, but due to some problems with testNG and WebApplicationContext,
 * simple junit was used.
 *
 * @author emorenkov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CrossoverRestServiceApplication.class)
@WebAppConfiguration
public class CrossoverRestServiceApplicationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @Value("${rest.logger.request.rate-limiting.accessPerMin}")
    private int accessPerMin;


    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }


    @Test
    public void testRegisterInvalidJson() throws Exception {
        mockMvc.perform(
                post("/register").content("{}").contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterGarbage() throws Exception {
        mockMvc.perform(
                post("/register").content("not_json_content").contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegister() throws Exception {
        mockMvc.perform(
                post("/register").content("{\"display_name\":\"test\"}").contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/json;charset=UTF-8"))
               .andExpect(jsonPath("display_name", equalTo("test")))
               .andExpect(jsonPath("application_id", notNullValue()))
               .andExpect(jsonPath("display_name", notNullValue()));
    }

    @Test
    public void testAuthWithoutHeader() throws Exception {
        mockMvc.perform(post("/auth")).andExpect(status().isBadRequest());
    }

    @Test
    public void testAuthWithInvalidHeader() throws Exception {
        mockMvc.perform(post("/auth").header("Authorization", "some text")).andExpect(status().isBadRequest());
    }

    @Test
    public void testAuthWithInvalidApplication() throws Exception {
        mockMvc.perform(post("/auth").header("Authorization", "app_id:secret")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testAuth() throws Exception {
        Application application = getApplication();

        mockMvc.perform(post("/auth").header("Authorization",
                                             application.getApplicationId() + ":" + application.getSecret()))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/json;charset=UTF-8"))
               .andExpect(jsonPath("access_token", notNullValue()));
    }

    @Test
    public void testAuthSingleSession() throws Exception {
        Application application = getApplication();

        mockMvc.perform(post("/auth").header("Authorization",
                                             application.getApplicationId() + ":" + application.getSecret()));
        mockMvc.perform(post("/auth").header("Authorization",
                                             application.getApplicationId() + ":" + application.getSecret()))
               .andExpect(status().isBadRequest());

    }

    @Test
    public void testLogInvalidAccessTokenEmptyContent() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                post("/log")
                        .header("Authorization", "invalid_token")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                                     .andReturn();

        mvcResult.getAsyncResult();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isBadRequest());
    }

    /**
     * Application with such id doesn't exist.
     *
     * @throws Exception
     */
    @Test
    public void testLogApplicationNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/log")
                                                      .header("Authorization", "invalid_token")
                                                      .content("{\"application_id\" : \"app_id\",\n"
                                                               + " \"logger\" : \"loggerName\",\n"
                                                               + " \"level\" : \"Error\",\n"
                                                               + " \"message\" : \"message\"}")
                                                      .contentType(MediaType.APPLICATION_JSON))
                                     .andReturn();

        mvcResult.getAsyncResult();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isForbidden());
    }

    @Test
    public void testLogInvalidToken() throws Exception {
        Application application = getApplication();

        MvcResult mvcResult = mockMvc.perform(
                post("/log")
                        .header("Authorization", "invalid_token")
                        .content("{\"application_id\" : \"" + application.getApplicationId() + "\",\n"
                                 + " \"logger\" : \"loggerName\",\n"
                                 + " \"level\" : \"Error\",\n"
                                 + " \"message\" : \"message\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                                     .andReturn();

        mvcResult.getAsyncResult();
        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isForbidden());
    }

    @Test
    public void testLog() throws Exception {
        Application application = getApplication();

        AuthResponse authorizeApplication = getAuthResponseForApplication(application);
        MvcResult mvcResult = mockMvc.perform(
                post("/log")
                        .header("Authorization", authorizeApplication.getAccessToken())
                        .content("{\"application_id\" : \"" + application.getApplicationId() + "\",\n"
                                 + " \"logger\" : \"loggerName\",\n"
                                 + " \"level\" : \"Error\",\n"
                                 + " \"message\" : \"message\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                                     .andReturn();

        mvcResult.getAsyncResult();
        mockMvc.perform(asyncDispatch(mvcResult))
               .andExpect(status().isOk())
               .andExpect(content().contentType("application/json;charset=UTF-8"))
               .andExpect(jsonPath("success", equalTo(true)));
    }

    @Test
    public void testLogRateLimitExceed() throws Exception {
        Application application = getApplication();

        AuthResponse authorizeApplication = getAuthResponseForApplication(application);
        for (int i = 0; i < accessPerMin; i++) {
            MvcResult mvcResult = mockMvc.perform(post("/log")
                                                          .header("Authorization",
                                                                  authorizeApplication.getAccessToken())
                                                          .content("{\"application_id\" : \"" + application
                                                                  .getApplicationId() + "\",\n"
                                                                   + " \"logger\" : \"loggerName\",\n"
                                                                   + " \"level\" : \"Error\",\n"
                                                                   + " \"message\" : \"message\"}")
                                                          .contentType(MediaType.APPLICATION_JSON)).andReturn();
            mvcResult.getAsyncResult();
            mockMvc.perform(asyncDispatch(mvcResult))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType("application/json;charset=UTF-8"));
        }
        MvcResult mvcResult = mockMvc.perform(
                post("/log")
                        .header("Authorization", authorizeApplication.getAccessToken())
                        .content("{\"application_id\" : \"" + application.getApplicationId() + "\",\n"
                                 + " \"logger\" : \"loggerName\",\n"
                                 + " \"level\" : \"Error\",\n"
                                 + " \"message\" : \"message\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                                     .andReturn();

        mvcResult.getAsyncResult();
        mockMvc.perform(asyncDispatch(mvcResult))
               .andExpect(status().isForbidden())
               .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }





    private Application getApplication() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                post("/register").content("{\"display_name\":\"test\"}").contentType(MediaType.APPLICATION_JSON))
                                     .andReturn();
        return mapper.readValue(mvcResult.getResponse().getContentAsString(), Application.class);
    }

    private AuthResponse getAuthResponseForApplication(Application application) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/auth").header("Authorization",
                                                                   application.getApplicationId() + ":"
                                                                   + application.getSecret())).andReturn();
        return mapper.readValue(mvcResult.getResponse().getContentAsString(), AuthResponse.class);
    }

}