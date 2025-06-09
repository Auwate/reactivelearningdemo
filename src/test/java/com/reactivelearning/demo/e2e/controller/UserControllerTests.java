package com.reactivelearning.demo.e2e.controller;

import com.reactivelearning.demo.dto.auth.LoginRequest;
import com.reactivelearning.demo.dto.auth.RegisterRequest;
import com.reactivelearning.demo.service.MfaService;
import com.reactivelearning.demo.service.UserService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An End-To-End testing suite to target controller behavior.
 * - DirtiesContext not used as it causes issues with R2DBC connection pooling. This becomes especially apparent
 * when trying to get valid MFA codes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class UserControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerTests.class);

    private final String LOGIN_URI = "/api/v1/auth/login";
    private final String REGISTER_URI = "/api/v1/auth/register";

    private final WebTestClient webTestClient;
    private final MfaService mfaService;
    private final UserService userService;
    private final GoogleAuthenticator authenticator;

    @Autowired
    public UserControllerTests(
            WebTestClient webTestClient,
            MfaService mfaService,
            UserService userService,
            GoogleAuthenticator authenticator) {
        this.webTestClient = webTestClient;
        this.mfaService = mfaService;
        this.userService = userService;
        this.authenticator = authenticator;
    }

    /**
     * Test that the system works correctly and the controller returns a 200.
     */
    @Test
    void shouldReturn200WhenRegisteringWithValidData() {

        logger.info("shouldReturn200WhenRegisteringWithValidData: Starting");

        RegisterRequest registerRequest = new RegisterRequest(
                "auwatetest",
                "testpassword",
                "test@email.com"
        );

        webTestClient.post()
                .uri(REGISTER_URI)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class);

    }

    /**
     * When trying to register, the controller should return a 4XX if given a duplicate username
     */
    @Test
    void shouldReturn400WhenRegisteringWithDuplicateName() {

        logger.info("shouldReturn400WhenRegisteringWithDuplicateName: Starting");

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

        logger.info("shouldReturn400WhenRegisteringWithAWeakPassword: Starting");

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

        logger.info("shouldReturn200WhenLoggingInWithValidCredentials: Starting");

        RegisterRequest registerRequest = new RegisterRequest(
                "auwate",
                "testpassword",
                "email");
        LoginRequest loginRequest;
        String encodedUser = "YXV3YXRlOnRlc3RwYXNzd29yZA==";

        // Register user
        String response = webTestClient.post()
                .uri(REGISTER_URI)
                .bodyValue(registerRequest)
                .exchange()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        assertNotNull(response);

        String secret = response.substring(response.indexOf("secret=")+7, response.indexOf("&issuer="));
        loginRequest = new LoginRequest(String.valueOf(authenticator.getTotpPassword(secret)));

        // Assertion
        webTestClient.post()
                .uri(LOGIN_URI)
                .bodyValue(loginRequest)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedUser)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectCookie().httpOnly("reactive_authn_authz", true)
                .expectCookie().sameSite("Strict", "Strict")
                .expectCookie().secure("reactive_authn_authz", true);

    }

    /**
     * Test that trying to log in with an unknown user leads to a 4XX response
     */
    @Test
    void shouldReturn401WhenLoggingInWithInvalidUser() {

        logger.info("shouldReturn401WhenLoggingInWithInvalidUser: Starting");

        LoginRequest loginRequest = new LoginRequest("123456");
        String encodedUnknownUser = "d3Jvbmc6cGFzc3dvcmQ=";

        webTestClient.post()
                .uri(LOGIN_URI)
                .bodyValue(loginRequest)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedUnknownUser)
                .exchange()
                .expectStatus().is4xxClientError();

    }

}
