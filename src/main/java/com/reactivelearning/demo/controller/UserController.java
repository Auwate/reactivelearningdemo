package com.reactivelearning.demo.controller;

import com.reactivelearning.demo.dto.auth.LoginRequest;
import com.reactivelearning.demo.dto.auth.RegisterRequest;
import com.reactivelearning.demo.dto.user.PartialUserDTO;
import com.reactivelearning.demo.dto.user.UserDTO;
import com.reactivelearning.demo.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Controller method for /login
     * @param loginRequest Object of LoginRequest. Holds the TOTP code for authenticating a user if they have
     *                     totp enabled.
     * @return Void : The controller returns a status code and related authentication cookies.
     */
    @PostMapping("/auth/login")
    public Mono<ResponseEntity<Void>> login(
            @RequestBody @Valid LoginRequest loginRequest
    ) {
        return userService.login(loginRequest)
                .doOnSubscribe(sub -> logger.info("POST connection received at /api/v1/login"))
                .map(loginResponse -> {
                    if (loginResponse.requires2fa()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    } else if (loginResponse.invalid2fa()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    } else if (loginResponse.success()) {
                        return ResponseEntity.status(HttpStatus.OK).build();
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                });
    }

    /**
     * Controller method for /register.
     * @param registerRequest Object of RegisterRequest. Holds data for registering a user.
     * @return Void : The controller returns a status code and related authentication cookies.
     */
    @PostMapping("/auth/register")
    public Mono<ResponseEntity<Void>> register(
            @RequestBody @Valid RegisterRequest registerRequest
    ) {
        return userService.register(registerRequest)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).build());
    }

    @PostMapping("/users")
    public Mono<ResponseEntity<Map<UUID, UserDTO>>> createUser(
            @RequestBody @Valid UserDTO userDTO
    ) {
        logger.info("POST connection received at /api/v1/users");
        return userService.addUser(userDTO)
                .map(createdUsers -> ResponseEntity.ok(createdUsers));
    }

    @GetMapping("/users")
    public Mono<ResponseEntity<Flux<Map<UUID, UserDTO>>>> getUsers() {
        logger.info("GET connection received at /api/v1/users");
        return Mono.fromSupplier(() -> ResponseEntity.ok(
                userService.getUsers()));
    }

    @DeleteMapping(value = "/users/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") UUID id) {
        logger.info("DELETE connection received at /api/v1/users/{}", id);
        return userService.deleteUser(id)
                .then(Mono.fromSupplier(() -> ResponseEntity
                        .status(HttpStatus.OK)
                        .build()));
    }

    @PutMapping(value = "/users/{id}")
    public Mono<ResponseEntity<Map<UUID, UserDTO>>> updateUser(
            @RequestBody @Valid PartialUserDTO partialUserDTO, @PathVariable("id") UUID id) {
        logger.info("PUT connection received at /api/v1/users/{}", id);
        return userService.updateUser(id, partialUserDTO)
                .map(updatedUser -> ResponseEntity.ok(
                        Map.of(id, updatedUser)));
    }

    @GetMapping("/csrf")
    public Mono<ResponseEntity<Void>> csrf(ServerWebExchange exchange) {
        logger.info("GET connection received at /api/v1/csrf");
        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.OK).build());
    }

}
