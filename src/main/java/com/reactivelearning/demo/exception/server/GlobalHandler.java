package com.reactivelearning.demo.exception.server;

import com.reactivelearning.demo.exception.entities.ExistsException;
import com.reactivelearning.demo.exception.entities.NotFoundException;
import com.reactivelearning.demo.exception.entities.WeakPasswordException;
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
        logger.error("ConstraintViolationException handled");
        String response = "Please check your provided JSON data and try again.";
        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    public Mono<ResponseEntity<String>> handleNotFoundException(NotFoundException ex) {
        logger.error("Could not find user.");
        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
    }

    @ExceptionHandler(ExistsException.class)
    public Mono<ResponseEntity<String>> handleExistsException(ExistsException ex) {
        logger.error("Username exists.");
        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
    }

    @ExceptionHandler(WeakPasswordException.class)
    public Mono<ResponseEntity<String>> handleWeakPasswordException(WeakPasswordException ex) {
        logger.error("Password should be at least 8 characters long.");
        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleGenericException(Exception ex) {
        logger.error("Exception: {} Type: {}", ex.getMessage(), ex.getClass());
        return Mono.fromSupplier(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
    }

}
