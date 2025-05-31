package com.reactivelearning.demo.controller;

import com.reactivelearning.demo.dto.PartialUserDTO;
import com.reactivelearning.demo.dto.UserDTO;
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

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/users")
    public Mono<ResponseEntity<Map<UUID, UserDTO>>> createUser(
            @RequestBody @Valid UserDTO userDTO) {
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
