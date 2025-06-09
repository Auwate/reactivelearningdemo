package com.reactivelearning.demo.exception.entities;

public class MfaNotFoundException extends RuntimeException {
    public MfaNotFoundException(String message) {
        super(message);
    }
}
