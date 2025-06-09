package com.reactivelearning.demo.exception.entities;

public class TOTPInvalidException extends RuntimeException {
    public TOTPInvalidException(String message) {
        super(message);
    }
}
