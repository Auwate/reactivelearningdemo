package com.reactivelearning.demo.integration.service;

import com.reactivelearning.demo.dto.auth.RegisterRequest;
import com.reactivelearning.demo.dto.user.UserRequest;
import com.reactivelearning.demo.entities.Mfa;
import com.reactivelearning.demo.entities.RoleType;
import com.reactivelearning.demo.entities.User;
import com.reactivelearning.demo.exception.entities.ExistsException;
import com.reactivelearning.demo.exception.entities.NotFoundException;
import com.reactivelearning.demo.exception.entities.WeakPasswordException;
import com.reactivelearning.demo.repository.user.RolesRepository;
import com.reactivelearning.demo.repository.user.UsersRepository;
import com.reactivelearning.demo.repository.user.UsersRolesRepository;
import com.reactivelearning.demo.security.util.PasswordHandler;
import com.reactivelearning.demo.service.MfaService;
import com.reactivelearning.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceTests {

    private final Logger logger = LoggerFactory.getLogger(UserServiceTests.class);

    private final PasswordHandler passwordHandler;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UsersRolesRepository usersRolesRepository;
    private final UserService userService;
    private final MfaService mfaService;

    @Autowired
    public UserServiceTests(
            PasswordHandler passwordHandler,
            UsersRepository usersRepository,
            RolesRepository rolesRepository,
            UsersRolesRepository usersRolesRepository,
            TransactionalOperator transactionalOperator
    ) {
        this.passwordHandler = passwordHandler;
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.usersRolesRepository = usersRolesRepository;
        this.mfaService = mock(MfaService.class);           // MfaService is mocked.
        this.userService = new UserService(
                usersRepository,
                rolesRepository,
                usersRolesRepository,
                passwordHandler,
                transactionalOperator,
                mfaService
        );
    }

    /**
     * Test that the service layer can successfully create a user, role, and userRole
     */
    @Test
    void shouldCreateUserWhenDataIsValid() {

        logger.info("shouldCreateUserWhenDataIsValid: Starting");

        RegisterRequest registerRequest = new RegisterRequest("Test", "TestPassword", "Test3");
        UserRequest userDTO = UserRequest.fromRegisterRequest(registerRequest, RoleType.USER);

        Mfa mfa = Mfa.of(UUID.randomUUID(), false, "");
        when(mfaService.createMfa(any(User.class))).thenReturn(Mono.just(mfa));

        Mono<User> request = userService.createUser(userDTO);

        logger.info("shouldCreateUserWhenDataIsValid: Test 1");

        StepVerifier.create(request)
                .expectNextMatches(result -> {
                    assertEquals(userDTO.getUsername(), result.getUsername(), String.format("Expected %s, Got %s", userDTO.getUsername(), result.getUsername()));
                    assertTrue(passwordHandler.compare(userDTO.getPassword(), result.getPassword()), String.format("Expected %s, Got %s", userDTO.getPassword(), result.getPassword()));
                    assertEquals(userDTO.getEmail(), result.getEmail(), String.format("Expected %s, Got %s", userDTO.getEmail(), result.getEmail()));
                    assertEquals(RoleType.USER.name(), result.getRoles().getFirst().getRole());
                    verify(mfaService, times(1)).createMfa(any(User.class));

                    return true;
                })
                .expectComplete()
                .verify();

        logger.info("shouldCreateUserWhenDataIsValid: Test 2");

        StepVerifier.create(usersRepository.findByUsername(userDTO.getUsername())
                .switchIfEmpty(Mono.error(new NotFoundException("User does not exist.")))
                .flatMap(foundUser -> rolesRepository.findByUserId(foundUser.getId())
                        .switchIfEmpty(Mono.error(new NotFoundException("Roles does not exist.")))
                        .collectList()
                        .flatMap(foundRoles ->
                                usersRolesRepository.findByUsersIdAndRolesId(
                                        foundUser.getId(), foundRoles.getFirst().getId()
                                )
                                .switchIfEmpty(Mono.error(new NotFoundException("UserRoles does not exist."))))))
                .expectNextCount(1)
                .expectComplete()
                .verify();

    }

    /**
     * Test that the service layer will fail on a weak password
     */
    @Test
    void shouldFailToCreateUserWhenPasswordIsWeak() {

        logger.info("shouldFailToCreateUserWhenPasswordIsWeak: Starting");

        RegisterRequest registerRequest = new RegisterRequest("Test2", "Weak", "Test3");
        UserRequest userDTO = UserRequest.fromRegisterRequest(registerRequest, RoleType.USER);

        Mono<User> request = userService.createUser(userDTO);

        StepVerifier.create(request)
                .expectErrorMatches(exception -> exception instanceof WeakPasswordException)
                .verify();

    }

    /**
     * Test that the service layer will fail on a duplicate name
     */
    @Test
    void shouldFailOnDuplicateName() {

        logger.info("shouldFailOnDuplicateName: Starting");

        RegisterRequest registerRequest = new RegisterRequest("Test", "TestPassword", "Test3");
        UserRequest userDTO = UserRequest.fromRegisterRequest(registerRequest, RoleType.USER);

        Mfa mfa = Mfa.of(UUID.randomUUID(), false, "");
        when(mfaService.createMfa(any(User.class))).thenReturn(Mono.just(mfa));

        Mono<User> request = userService.createUser(userDTO);

        StepVerifier.create(request)
                .expectNextCount(1)
                .expectComplete()
                .verify();

        StepVerifier.create(request)
                .expectErrorMatches(exception -> exception instanceof ExistsException)
                .verify();

    }

}
