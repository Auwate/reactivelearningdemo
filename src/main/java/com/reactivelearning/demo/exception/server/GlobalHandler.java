package com.reactivelearning.demo.exception.server;

import com.reactivelearning.demo.exception.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<String>> handleValidationException(WebExchangeBindException ex) {
        return Mono.fromSupplier(() -> ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Please check your provided JSON data and try again."))
                .doOnSubscribe(sub -> logger.error("ConstraintViolationException handled"));
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<String>> handleNotFoundException(NotFoundException ex) {
        return Mono.fromSupplier(() -> ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.error("User could not be found."));
    }

    @ExceptionHandler(ExistsException.class)
    public Mono<ResponseEntity<String>> handleExistsException(ExistsException ex) {
        return Mono.fromSupplier(() -> ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.error("User tried registering with duplicate name."));
    }

    @ExceptionHandler(WeakPasswordException.class)
    public Mono<ResponseEntity<String>> handleWeakPasswordException(WeakPasswordException ex) {
        return Mono.fromSupplier(
                () -> ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.error(
                        "User tried registering with a password <= 8 characters long."
                ));
    }

    @ExceptionHandler(RolesNotFoundException.class)
    public Mono<ResponseEntity<String>> handleRolesNotFoundException(RolesNotFoundException ex) {
        return Mono.fromSupplier(() -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.error("User with no role was found."));
    }

    @ExceptionHandler(InternalServerException.class)
    public Mono<ResponseEntity<String>> handleInternalServerException(InternalServerException ex) {
        return Mono.fromSupplier(() ->
                ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.error("An internal server error occurred."));
    }

    @ExceptionHandler(MfaNotFoundException.class)
    public Mono<ResponseEntity<String>> handleMfaNotFoundException(MfaNotFoundException ex) {
        return Mono.fromSupplier(() ->
                ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.error("The users MFA could not be found."));
    }

    @ExceptionHandler(TOTPInvalidException.class)
    public Mono<ResponseEntity<String>> handleTOTPInvalidException(TOTPInvalidException ex) {
        return Mono.fromSupplier(() ->
                ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.info("TOTP incorrect."));
    }

    @ExceptionHandler(MfaRepositoryException.class)
    public Mono<ResponseEntity<String>> handleMfaRepositoryException(MfaRepositoryException ex) {
        return Mono.fromSupplier(() ->
                        ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.info("There was an " +
                        "error manipulating the MFA object in the repository"));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleGenericException(Exception ex) {
        return Mono.fromSupplier(
                () -> ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ex.getMessage()))
                .doOnSubscribe(sub -> logger.error(
                        "Exception: {} Type: {}", ex.getMessage(), ex.getClass()));
    }

}
