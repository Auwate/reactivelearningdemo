package com.reactivelearning.demo.e2e.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.reactivelearning.demo.dto.auth.LoginRequest;
import com.reactivelearning.demo.dto.auth.RegisterRequest;
import com.reactivelearning.demo.security.jwt.JwtUtil;
import com.reactivelearning.demo.service.MfaService;
import com.reactivelearning.demo.service.UserService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An End-To-End testing suite to target controller behavior.
 * - DirtiesContext not used as it causes issues with R2DBC connection pooling. This becomes especially apparent
 * when trying to get valid MFA codes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerTests {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerTests.class);

    private final String LOGIN_URI = "/api/v1/auth/login";
    private final String REGISTER_URI = "/api/v1/auth/register";
    private final String MUTATE_URI = "/api/v1/user";

    private final WebTestClient webTestClient;
    private final MfaService mfaService;
    private final UserService userService;
    private final GoogleAuthenticator authenticator;
    private final JwtUtil jwtUtil;
    private final String secret;

    @Autowired
    public UserControllerTests(
            WebTestClient webTestClient,
            MfaService mfaService,
            UserService userService,
            GoogleAuthenticator authenticator,
            JwtUtil jwtUtil,
            @Value("${jwt.secret}") String secret) {
        this.webTestClient = webTestClient;
        this.mfaService = mfaService;
        this.userService = userService;
        this.authenticator = authenticator;
        this.jwtUtil = jwtUtil;
        this.secret = secret;
    }

    private String getSecretCode(String otpCode) {
        return otpCode.substring(otpCode.indexOf("secret=")+7, otpCode.indexOf("&issuer="));
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

        String secretKey = getSecretCode(response);
        loginRequest = new LoginRequest(String.valueOf(authenticator.getTotpPassword(secretKey)));

        // Assertion
        webTestClient.post()
                .uri(LOGIN_URI)
                .bodyValue(loginRequest)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedUser)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectCookie().httpOnly("reactive_authn_authz", true)
                .expectCookie().sameSite("reactive_authn_authz", "Strict");

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

    /**
     * When hitting an endpoint that requires cookie auth, return 4XX when it is not provided
     */
    @Test
    void shouldReturn400WhenNotProvidingCookieAuth() {

        webTestClient.post()
                .uri(MUTATE_URI)
                .exchange()
                .expectStatus().is4xxClientError();

    }

    /**
     * If the provided JWT is invalid (wrong key), return 4XX
     */
    @Test
    void shouldReturn400WhenProvidedCookieAuthIsInvalid() {

        Algorithm algorithm = Algorithm.HMAC512("WRONG");
        String invalidJwt = JWT.create()
                .withExpiresAt(
                        new Date(
                                System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()))
                .sign(algorithm);

        webTestClient.post()
                .uri(MUTATE_URI)
                .header(HttpHeaders.COOKIE, invalidJwt)
                .exchange()
                .expectStatus().is4xxClientError();

    }

    /**
     * If the provided JWT is expired, return 4XX
     */
    @Test
    void shouldReturn400WhenProvidedExpiredCookieAuth() {

        Algorithm algorithm = Algorithm.HMAC512(secret);
        String expiredJwt = JWT.create()
                .sign(algorithm);

        webTestClient.post()
                .uri(MUTATE_URI)
                .header(HttpHeaders.COOKIE, expiredJwt)
                .exchange()
                .expectStatus().is4xxClientError();

    }

    /**
     * After registering and logging in, hitting this endpoint should return 200
     */
    @Test
    void shouldReturn200WhenProvidedValidCookie() {

        RegisterRequest registerRequest = new RegisterRequest(
                "auwate",
                "testpassword",
                "Test"
        );
        String encodedUser = "YXV3YXRlOnRlc3RwYXNzd29yZA==";

        String otpCode = webTestClient.post()
                .uri(REGISTER_URI)
                .bodyValue(registerRequest)
                .exchange()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        assertNotNull(otpCode);

        String secretKey = getSecretCode(otpCode);
        LoginRequest loginRequest = new LoginRequest(String.valueOf(authenticator.getTotpPassword(secretKey)));

        FluxExchangeResult<String> result = webTestClient.post()
                .uri(LOGIN_URI)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedUser)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class);

        ResponseCookie cookie = result.getResponseCookies().getFirst("reactive_authn_authz");

        assertNotNull(cookie);

        webTestClient.post()
                .uri(MUTATE_URI)
                .header(HttpHeaders.COOKIE, "reactive_authn_authz=" + cookie.getValue())
                .exchange()
                .expectStatus().is2xxSuccessful();

    }

}
