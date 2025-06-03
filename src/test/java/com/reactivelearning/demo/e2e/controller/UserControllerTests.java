package com.reactivelearning.demo.e2e.controller;

import com.reactivelearning.demo.controller.UserController;
import com.reactivelearning.demo.dto.auth.LoginRequest;
import com.reactivelearning.demo.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * An End-To-End testing suite to target controller behavior.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerTests.class);

    private final String LOGIN_URI = "/api/v1/auth/login";
    private final String REGISTER_URI = "/api/v1/auth/register";

    private final UserController userController;
    private final WebTestClient webTestClient;

    @Autowired
    public UserControllerTests(
            UserController userController,
            WebTestClient webTestClient) {
        this.userController = userController;
        this.webTestClient = webTestClient;
    }

    /**
     * Test that the system works correctly and the controller returns a 200.
     */
    @Test
    void shouldReturn200WhenRegisteringWithValidData() {

        logger.debug("shouldReturn200WhenRegisteringWithValidData: Starting");

        RegisterRequest registerRequest = new RegisterRequest(
                "auwate",
                "testpassword",
                "test@email.com"
        );

        webTestClient.post()
                .uri(REGISTER_URI)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().is2xxSuccessful();

    }

    /**
     * When trying to register, the controller should return a 4XX if given a duplicate username
     */
    @Test
    void shouldReturn400WhenRegisteringWithDuplicateName() {

        logger.debug("shouldReturn400WhenRegisteringWithDuplicateName: Starting");

        RegisterRequest registerRequest = new RegisterRequest(
                "test",
                "testpassword",
                "test@email.com"
        );

        webTestClient.post()
                .uri(REGISTER_URI)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().is2xxSuccessful();


        webTestClient.post()
                .uri(REGISTER_URI)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().is4xxClientError();

    }

    /**
     * When trying to register with a weak password (<8 characters), it should error
     */
    @Test
    void shouldReturn400WhenRegisteringWithAWeakPassword() {

        logger.debug("shouldReturn400WhenRegisteringWithAWeakPassword: Starting");

        RegisterRequest registerRequest = new RegisterRequest("Test", "Weak", "Test");

        webTestClient.post()
                .uri(REGISTER_URI)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().is4xxClientError();

    }

    /**
     * Test that login works correctly with valid data
     */
    @Test
    void shouldReturn200WhenLoggingInWithValidCredentials() {

        logger.debug("shouldReturn200WhenLoggingInWithValidCredentials: Starting");

        RegisterRequest registerRequest = new RegisterRequest("Test", "TestPassword", "Test");
        LoginRequest loginRequest = new LoginRequest("Test");
        String encodedUser = "VGVzdDpUZXN0UGFzc3dvcmQ=";

        // Create user
        webTestClient.post()
                .uri(REGISTER_URI)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().is2xxSuccessful();

        // Assertion
        webTestClient.post()
                .uri(LOGIN_URI)
                .bodyValue(loginRequest)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedUser)
                .exchange()
                .expectStatus().is2xxSuccessful();

    }

    /**
     * Test that trying to log in with an unknown user leads to a 4XX response
     */
    @Test
    void shouldReturn401WhenLoggingInWithInvalidUser() {

        logger.debug("shouldReturn401WhenLoggingInWithInvalidUser: Starting");

        LoginRequest loginRequest = new LoginRequest("Test");
        String encodedUnknownUser = "VGVzdDpUZXN0UGFzc3dvcmQ=";

        webTestClient.post()
                .uri(LOGIN_URI)
                .bodyValue(loginRequest)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedUnknownUser)
                .exchange()
                .expectStatus().is4xxClientError();

    }

}
