package com.reactivelearning.demo.integration.service;

import com.reactivelearning.demo.entities.Mfa;
import com.reactivelearning.demo.entities.User;
import com.reactivelearning.demo.exception.entities.MfaRepositoryException;
import com.reactivelearning.demo.repository.user.MfaRepository;
import com.reactivelearning.demo.repository.user.UsersRepository;
import com.reactivelearning.demo.service.MfaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MfaServiceTests {

    private final MfaService mfaService;
    private final MfaRepository mfaRepository;
    private final UsersRepository usersRepository;

    @Autowired
    public MfaServiceTests(
            UsersRepository usersRepository,
            MfaService mfaService,
            MfaRepository mfaRepository) {
        this.usersRepository = usersRepository;
        this.mfaService = mfaService;
        this.mfaRepository = mfaRepository;
    }

    private final Logger logger = LoggerFactory.getLogger(MfaServiceTests.class);

    /**
     * The MfaService class should create an MFA object in the database when given valid data
     */
    @Test
    void shouldCreateMfaWhenGivenValidData() {

        logger.info("shouldCreateMfaWhenGivenValidData: Starting");

        User user = new User("Test", "Test", "Test");

        logger.info("shouldCreateMfaWhenGivenValidData: Test 1");

        Mono<User> userRequest = this.usersRepository.save(user);
        Mono<Mfa> request = this.mfaService.createMfa(user);

        StepVerifier.create(userRequest)
                .expectNextMatches(response -> {
                    user.setId(response.getId());
                    return true;
                })
                .expectComplete()
                .verify();

        StepVerifier.create(request)
                .expectNextMatches(response -> {
                    assertFalse(response.isEnabled());
                    assertEquals(user.getId(), response.getUsersId());
                    return true;
                })
                .expectComplete()
                .verify();

    }

    @Test
    void shouldNotCreateMfaWhenGivenInvalidData() {

        logger.info("shouldNotCreateMfaWhenGivenInvalidData: Starting");

        User user = new User("Test", "Test", "Test");
        user.setId(UUID.randomUUID());

        StepVerifier
                .create(mfaService.createMfa(user))
                .expectErrorMatches(error -> error instanceof MfaRepositoryException)
                .verify();

    }

    @Test
    void shouldGetMfaWhenRetrievingValidData() {

        logger.info("shouldGetMfaWhenRetrievingValidData: Starting");

        User user = new User("Test", "Test", "Test");

        StepVerifier
                .create(usersRepository.save(user))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(mfaService.createMfa(user))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(mfaService.getMfa(user))
                .expectNextMatches(response -> {
                    assertEquals(user.getId(), response.getUsersId());
                    return true;
                })
                .verifyComplete();

    }

    @Test
    void shouldFailToGetMfaWhenRetrievingInvalidData() {

        logger.info("shouldFailToGetMfaWhenRetrievingInvalidData: Starting");

        User user = new User("Test", "Test", "Test");
        user.setId(UUID.randomUUID());

        StepVerifier
                .create(mfaService.getMfa(user))
                .expectErrorMatches(error -> error instanceof MfaRepositoryException)
                .verify();

    }

}
