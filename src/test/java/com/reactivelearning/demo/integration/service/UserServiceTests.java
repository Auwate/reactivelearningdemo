package com.reactivelearning.demo.integration.service;

import com.reactivelearning.demo.dto.UserDTO;
import com.reactivelearning.demo.entities.User;
import com.reactivelearning.demo.exception.entities.WeakPasswordException;
import com.reactivelearning.demo.security.util.PasswordHandler;
import com.reactivelearning.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class UserServiceTests {

    private final Logger logger = LoggerFactory.getLogger(UserServiceTests.class);

    private final UserService userService;
    private final PasswordHandler passwordHandler;

    @Autowired
    public UserServiceTests(
            UserService userService,
            PasswordHandler passwordHandler
    ) {
        this.userService = userService;
        this.passwordHandler = passwordHandler;
    }

    /**
     * Test that the service layer can successfully create a user, role, and userRole
     */
    @Test
    void shouldCreateUserWhenDataIsValid() {

        UserDTO userDTO = UserDTO.of("Test", "TestPassword", "Test3");

        Mono<Map<UUID, UserDTO>> request = userService.addUser(userDTO);

        StepVerifier.create(request)
                .expectNextMatches(result -> {
                    try {
                        UserDTO response = result
                                .values()
                                .stream()
                                .findFirst()
                                .orElseThrow(() -> new NoSuchElementException("User was not saved."));
                        assertEquals(userDTO.getUsername(), response.getUsername(), String.format("Expected %s, Got %s", userDTO.getUsername(),response.getUsername()));
                        assertTrue(passwordHandler.compare(userDTO.getPassword(), response.getPassword()), String.format("Expected %s, Got %s", userDTO.getPassword(), response.getPassword()));
                        assertEquals(userDTO.getEmail(), response.getEmail(), String.format("Expected %s, Got %s", userDTO.getEmail(), response.getEmail()));
                        return true;
                    } catch (NoSuchElementException exc) {
                        logger.error(exc.getMessage());
                        return false;
                    }
                })
                .expectComplete()
                .verify();

    }

    /**
     * Test that the service layer will fail on a weak password
     */
    @Test
    void shouldFailToCreateUserWhenPasswordIsWeak() {

        Mono<Map<UUID, UserDTO>> result = userService.addUser(
                UserDTO.of(
                        "Test2",
                        "Test",
                        "Test"));

        StepVerifier.create(result)
                .expectErrorMatches(exception -> exception instanceof WeakPasswordException)
                .verify();

    }

    /**
     * Test that the service layer returns a User given a valid username
     */
    @Test
    void shouldGetUserWithValidUsername() {

        Mono<User> request = userService.getUser("Test");

        StepVerifier.create(request)
                .expectNextMatches(response -> {
                    assertNotNull(response);
                    assertEquals("Test", response.getUsername());
                    return true;
                })
                .expectComplete()
                .verify();

    }

}
