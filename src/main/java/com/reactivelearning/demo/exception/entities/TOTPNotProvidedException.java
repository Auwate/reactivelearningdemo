package com.reactivelearning.demo.exception.entities;

public class TOTPNotProvidedException extends RuntimeException {
    public TOTPNotProvidedException(String message) {
        super(message);
    }
}
